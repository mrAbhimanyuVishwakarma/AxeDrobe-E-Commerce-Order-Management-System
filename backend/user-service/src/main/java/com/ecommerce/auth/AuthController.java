package com.ecommerce.auth;

import com.ecommerce.auth.dto.AuthOptions;
import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.GoogleLoginRequest;
import com.ecommerce.auth.dto.OtpLoginRequest;
import com.ecommerce.auth.dto.OtpRequest;
import com.ecommerce.auth.dto.OtpResponse;
import com.ecommerce.auth.dto.PasswordLoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.ResetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/options")
    public AuthOptions options() {
        return authService.options();
    }

    @PostMapping("/otp/request")
    public OtpResponse requestOtp(@Valid @RequestBody OtpRequest request) {
        return authService.requestOtp(request.identifier());
    }

    @PostMapping("/otp/verify")
    public AuthResponse loginWithOtp(@Valid @RequestBody OtpLoginRequest request) {
        return authService.loginWithOtp(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody PasswordLoginRequest request) {
        return authService.loginWithPassword(request);
    }

    @PostMapping("/password/reset")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/google")
    public AuthResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request.credential());
    }
}
