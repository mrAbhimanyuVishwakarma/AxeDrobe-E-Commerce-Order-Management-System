package com.ecommerce.auth;

import com.ecommerce.auth.GoogleTokenVerifier.GoogleProfile;
import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.OtpLoginRequest;
import com.ecommerce.auth.dto.PasswordLoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.security.JwtService;
import com.ecommerce.user.Role;
import com.ecommerce.user.User;
import com.ecommerce.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final OtpService otpService = mock(OtpService.class);
    private final GoogleTokenVerifier google = mock(GoogleTokenVerifier.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService("test-secret-that-is-long-enough-for-hs256!!", Duration.ofHours(1));
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, otpService, encoder, jwt, google, "owner@axedrobe.com");
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) u.setId("u-1");
            return u;
        });
    }

    @Test
    void otpLoginCreatesAccountForNewMobileNumber() {
        when(users.findByMobileNumber("9876543210")).thenReturn(Optional.empty());

        AuthResponse response = service.loginWithOtp(new OtpLoginRequest("+91 98765 43210", "123456", "Aarav"));

        verify(otpService).verify(new Identifier(Identifier.Type.MOBILE, "9876543210"), "123456");
        assertThat(response.user().mobileNumber()).isEqualTo("9876543210");
        assertThat(response.user().name()).isEqualTo("Aarav");
        assertThat(jwt.parse(response.token()).id()).isEqualTo("u-1");
    }

    @Test
    void registerRejectsExistingEmailBeforeConsumingCode() {
        when(users.findByEmail("riya@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.register(new RegisterRequest("Riya", "riya@example.com", "secret123", "123456")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(otpService, never()).verify(any(), anyString());
    }

    @Test
    void passwordLoginChecksHash() {
        User user = new User();
        user.setId("u-7");
        user.setEmail("riya@example.com");
        user.setPasswordHash(encoder.encode("secret123"));
        when(users.findByEmail("riya@example.com")).thenReturn(Optional.of(user));

        assertThat(service.loginWithPassword(new PasswordLoginRequest("riya@example.com", "secret123")).token()).isNotBlank();
        assertThatThrownBy(() -> service.loginWithPassword(new PasswordLoginRequest("riya@example.com", "wrong-pass")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void googleLoginLinksExistingEmailAccountAndPromotesAdmins() {
        User existing = new User();
        existing.setId("u-9");
        existing.setEmail("owner@axedrobe.com");
        when(google.verify("token")).thenReturn(new GoogleProfile("g-123", "owner@axedrobe.com", "Owner"));
        when(users.findByGoogleId("g-123")).thenReturn(Optional.empty());
        when(users.findByEmail("owner@axedrobe.com")).thenReturn(Optional.of(existing));

        AuthResponse response = service.loginWithGoogle("token");

        assertThat(existing.getGoogleId()).isEqualTo("g-123");
        assertThat(existing.getRole()).isEqualTo(Role.ADMIN);
        assertThat(jwt.parse(response.token()).isAdmin()).isTrue();
    }
}
