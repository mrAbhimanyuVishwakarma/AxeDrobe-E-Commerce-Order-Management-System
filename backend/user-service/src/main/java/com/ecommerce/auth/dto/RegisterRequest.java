package com.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Enter your name") @Size(max = 80) String name,
        @NotBlank(message = "Enter your email") @Email(message = "Enter a valid email address") String email,
        @NotBlank(message = "Enter a password") @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String password,
        @NotBlank(message = "Enter the 6-digit code") @Pattern(regexp = "[0-9]{6}", message = "Enter the 6-digit code") String otp) {
}
