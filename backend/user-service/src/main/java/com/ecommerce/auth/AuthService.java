package com.ecommerce.auth;

import com.ecommerce.auth.GoogleTokenVerifier.GoogleProfile;
import com.ecommerce.auth.dto.AuthOptions;
import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.OtpLoginRequest;
import com.ecommerce.auth.dto.OtpResponse;
import com.ecommerce.auth.dto.PasswordLoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.ResetPasswordRequest;
import com.ecommerce.security.JwtService;
import com.ecommerce.user.Role;
import com.ecommerce.user.User;
import com.ecommerce.user.UserRepository;
import com.ecommerce.user.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String BAD_CREDENTIALS = "Incorrect email/mobile number or password";

    private final UserRepository users;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleVerifier;
    private final Set<String> adminEmails;

    public AuthService(UserRepository users,
                       OtpService otpService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       GoogleTokenVerifier googleVerifier,
                       @Value("${app.admin-emails:}") String adminEmails) {
        this.users = users;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleVerifier = googleVerifier;
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    public AuthOptions options() {
        return new AuthOptions(
                googleVerifier.clientId(),
                otpService.canDeliver(Identifier.Type.EMAIL) || otpService.isDemoMode(),
                otpService.canDeliver(Identifier.Type.MOBILE) || otpService.isDemoMode(),
                otpService.isDemoMode());
    }

    public OtpResponse requestOtp(String rawIdentifier) {
        Identifier identifier = Identifier.parse(rawIdentifier);
        boolean newUser = findUser(identifier).isEmpty();
        String demoCode = otpService.send(identifier);
        return new OtpResponse(identifier.type().name(), identifier.masked(), newUser,
                otpService.resendCooldownSeconds(), demoCode);
    }

    /** Sign in with a one-time code. The first successful sign-in creates the account. */
    public AuthResponse loginWithOtp(OtpLoginRequest request) {
        Identifier identifier = Identifier.parse(request.identifier());
        otpService.verify(identifier, request.otp());
        User user = findUser(identifier).orElseGet(() -> newUser(identifier, request.name()));
        return signIn(user);
    }

    /** Email + password sign up. The email is confirmed with a one-time code first. */
    public AuthResponse register(RegisterRequest request) {
        Identifier identifier = Identifier.parse(request.email());
        if (!identifier.isEmail()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        }
        if (users.findByEmail(identifier.value()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An account with this email already exists. Please sign in instead.");
        }
        otpService.verify(identifier, request.otp());

        User user = newUser(identifier, request.name());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return signIn(user);
    }

    public AuthResponse loginWithPassword(PasswordLoginRequest request) {
        Identifier identifier = Identifier.parse(request.identifier());
        User user = findUser(identifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS));
        if (!user.hasPassword()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "This account doesn't have a password yet. Sign in with a code or Google, or set one with 'Forgot password'.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }
        return signIn(user);
    }

    /** Sets a new password after confirming a one-time code. Also works for accounts that never had one. */
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        Identifier identifier = Identifier.parse(request.identifier());
        User user = findUser(identifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "We couldn't find an account with that email or mobile number."));
        otpService.verify(identifier, request.otp());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return signIn(user);
    }

    public AuthResponse loginWithGoogle(String credential) {
        GoogleProfile profile = googleVerifier.verify(credential);
        User user = users.findByGoogleId(profile.googleId())
                .or(() -> users.findByEmail(profile.email()))
                .orElseGet(() -> newUser(new Identifier(Identifier.Type.EMAIL, profile.email()), profile.name()));
        user.setGoogleId(profile.googleId());
        return signIn(user);
    }

    private Optional<User> findUser(Identifier identifier) {
        return identifier.isEmail()
                ? users.findByEmail(identifier.value())
                : users.findByMobileNumber(identifier.value());
    }

    private User newUser(Identifier identifier, String name) {
        User user = new User();
        if (identifier.isEmail()) {
            user.setEmail(identifier.value());
        } else {
            user.setMobileNumber(identifier.value());
        }
        user.setName(StringUtils.hasText(name) ? name.trim() : defaultName(identifier));
        user.setCreatedAt(Instant.now());
        return user;
    }

    private static String defaultName(Identifier identifier) {
        if (!identifier.isEmail()) {
            return "Customer";
        }
        String local = identifier.value().substring(0, identifier.value().indexOf('@')).replaceAll("[._\\d]+", " ").trim();
        return local.isEmpty() ? "Customer" : Character.toUpperCase(local.charAt(0)) + local.substring(1);
    }

    private AuthResponse signIn(User user) {
        if (user.getEmail() != null && adminEmails.contains(user.getEmail())) {
            user.setRole(Role.ADMIN);
        }
        user.setLastLoginAt(Instant.now());
        User saved = users.save(user);
        return new AuthResponse(jwtService.issue(saved), UserResponse.from(saved));
    }
}
