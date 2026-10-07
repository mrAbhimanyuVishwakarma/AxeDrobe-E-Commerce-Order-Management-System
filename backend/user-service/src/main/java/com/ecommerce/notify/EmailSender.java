package com.ecommerce.notify;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;

/**
 * Sends sign-in codes by email, through the Brevo API when BREVO_API_KEY is set (works on hosts that
 * block SMTP), otherwise over SMTP with any provider (Gmail app password, Zoho, SES...).
 */
@Slf4j
@Component
public class EmailSender {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final BrevoClient brevo;
    private final String host;
    private final String from;
    private final String brandName;

    public EmailSender(ObjectProvider<JavaMailSender> mailSender,
                       BrevoClient brevo,
                       @Value("${spring.mail.host:}") String host,
                       @Value("${app.mail.from:}") String from,
                       @Value("${app.brand-name:AxeDrobe}") String brandName) {
        this.mailSender = mailSender;
        this.brevo = brevo;
        this.host = host;
        this.from = from;
        this.brandName = brandName;
    }

    public boolean isConfigured() {
        boolean smtp = StringUtils.hasText(host) && mailSender.getIfAvailable() != null;
        return StringUtils.hasText(from) && (brevo.isConfigured() || smtp);
    }

    public void sendOtp(String to, String code) {
        String subject = code + " is your " + brandName + " verification code";
        String text = "Your " + brandName + " verification code is " + code
                + ". It expires in 10 minutes. If you didn't ask for this, you can ignore this email.";
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:0 auto;padding:24px;color:#171717">
                  <h2 style="margin:0 0 16px;letter-spacing:2px">%s</h2>
                  <p style="margin:0 0 16px">Use this code to continue signing in:</p>
                  <p style="font-size:32px;font-weight:bold;letter-spacing:8px;margin:0 0 16px">%s</p>
                  <p style="margin:0 0 8px;color:#4d4d4d">The code expires in 10 minutes.</p>
                  <p style="margin:0;color:#888;font-size:13px">If you didn't request this, you can safely ignore this email.</p>
                </div>
                """.formatted(brandName.toUpperCase(), code);
        send(to, subject, text, html);
    }

    private void send(String to, String subject, String text, String html) {
        try {
            if (brevo.isConfigured()) {
                brevo.send(from, brandName, to, subject, html, text);
                return;
            }
            JavaMailSender sender = mailSender.getObject();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, brandName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, html);
            sender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException | RestClientException e) {
            log.error("Could not send email: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "We couldn't send the email right now. Please try again in a moment.");
        }
    }
}
