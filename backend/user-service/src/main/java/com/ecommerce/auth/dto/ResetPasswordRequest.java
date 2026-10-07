package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Enter your email or mobile number") String identifier,
        @NotBlank(message = "Enter the 6-digit code") @Pattern(regexp = "[0-9]{6}", message = "Enter the 6-digit code") String otp,
        @NotBlank(message = "Enter a new password") @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String newPassword) {
}
