package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordLoginRequest(
        @NotBlank(message = "Enter your email or mobile number") String identifier,
        @NotBlank(message = "Enter your password") String password) {
}
