package com.ecommerce.auth;

import com.ecommerce.notify.EmailSender;
import com.ecommerce.notify.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
public class OtpService {

    static final Duration CODE_TTL = Duration.ofMinutes(10);
    static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
    static final int MAX_SENDS_PER_HOUR = 5;
    static final int MAX_ATTEMPTS = 5;

    private final MongoTemplate mongo;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final boolean demoMode;
    private final SecureRandom random = new SecureRandom();

    public OtpService(MongoTemplate mongo,
                      PasswordEncoder passwordEncoder,
                      EmailSender emailSender,
                      SmsSender smsSender,
                      @Value("${app.otp.demo-mode:false}") boolean demoMode) {
        this.mongo = mongo;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.smsSender = smsSender;
        this.demoMode = demoMode;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public boolean canDeliver(Identifier.Type type) {
        return type == Identifier.Type.EMAIL ? emailSender.isConfigured() : smsSender.isConfigured();
    }

    public int resendCooldownSeconds() {
        return (int) RESEND_COOLDOWN.toSeconds();
    }

    /**
     * Generates and delivers a new code. Returns the code itself only in demo mode,
     * where it is shown on screen instead of (or as well as) being sent.
     */
    public String send(Identifier identifier) {
        boolean deliverable = canDeliver(identifier.type());
        if (!deliverable && !demoMode) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, identifier.isEmail()
                    ? "Email codes are not available right now. Please try another sign-in method."
                    : "SMS codes are not available right now. Please use your email instead.");
        }

        Instant now = Instant.now();
        OtpChallenge challenge = mongo.findById(identifier.key(), OtpChallenge.class);
        if (challenge != null) {
            long wait = Duration.between(now, challenge.getLastSentAt().plus(RESEND_COOLDOWN)).toSeconds();
            if (wait > 0) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Please wait " + wait + " seconds before requesting a new code.");
            }
            if (challenge.getSendCount() >= MAX_SENDS_PER_HOUR) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many codes requested. Please try again in an hour.");
            }
        } else {
            challenge = new OtpChallenge();
            challenge.setIdentifier(identifier.key());
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        if (deliverable) {
            if (identifier.isEmail()) {
                emailSender.sendOtp(identifier.value(), code);
            } else {
                smsSender.sendOtp(identifier.value(), code);
            }
        }

        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setExpiresAt(now.plus(CODE_TTL));
        challenge.setAttempts(0);
        challenge.setSendCount(challenge.getSendCount() + 1);
        challenge.setLastSentAt(now);
        mongo.save(challenge);
        log.info("OTP sent to {} via {}", identifier.masked(), deliverable ? identifier.type() : "demo mode");

        return demoMode ? code : null;
    }

    /**
     * Checks a code and consumes it on success. The attempt counter is incremented atomically
     * before comparing, so parallel guesses cannot get past the attempt limit.
     */
    public void verify(Identifier identifier, String code) {
        Query query = Query.query(Criteria.where("_id").is(identifier.key()).and("attempts").lt(MAX_ATTEMPTS));
        OtpChallenge challenge = mongo.findAndModify(query, new Update().inc("attempts", 1),
                FindAndModifyOptions.options().returnNew(true), OtpChallenge.class);

        if (challenge == null) {
            boolean exists = mongo.exists(Query.query(Criteria.where("_id").is(identifier.key())), OtpChallenge.class);
            throw exists
                    ? new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many incorrect attempts. Please request a new code.")
                    : new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active code. Please request a new one.");
        }
        if (Instant.now().isAfter(challenge.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This code has expired. Please request a new one.");
        }
        if (code == null || !passwordEncoder.matches(code, challenge.getCodeHash())) {
            int left = MAX_ATTEMPTS - challenge.getAttempts();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, left > 0
                    ? "Incorrect code. " + left + (left == 1 ? " attempt" : " attempts") + " left."
                    : "Incorrect code. Please request a new one.");
        }
        mongo.remove(challenge);
    }
}
