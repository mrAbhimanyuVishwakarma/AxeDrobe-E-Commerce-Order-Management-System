package com.ecommerce.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

/**
 * Verifies the ID token returned by "Sign in with Google" on the frontend.
 */
@Component
public class GoogleTokenVerifier {

    private static final String GOOGLE_CERTS = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    private final String clientId;
    private final JwtDecoder decoder;

    public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId;
        this.decoder = StringUtils.hasText(clientId) ? buildDecoder(clientId) : null;
    }

    public boolean isConfigured() {
        return decoder != null;
    }

    public String clientId() {
        return isConfigured() ? clientId : null;
    }

    public GoogleProfile verify(String credential) {
        if (!isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google sign-in is not enabled.");
        }
        Jwt jwt;
        try {
            jwt = decoder.decode(credential);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google sign-in failed. Please try again.");
        }
        String email = jwt.getClaimAsString("email");
        if (email == null || !Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your Google account email is not verified.");
        }
        return new GoogleProfile(jwt.getSubject(), email.toLowerCase(Locale.ROOT), jwt.getClaimAsString("name"));
    }

    private static JwtDecoder buildDecoder(String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_CERTS).build();
        OAuth2TokenValidator<Jwt> issuer = jwt -> ISSUERS.contains(jwt.getClaimAsString("iss"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Unexpected issuer", null));
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience().contains(clientId)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token was issued for another app", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), issuer, audience));
        return decoder;
    }

    public record GoogleProfile(String googleId, String email, String name) {
    }
}
