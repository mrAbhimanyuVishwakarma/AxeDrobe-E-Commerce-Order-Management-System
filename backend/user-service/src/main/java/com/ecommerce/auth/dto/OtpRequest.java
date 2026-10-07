package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(@NotBlank(message = "Enter your email or mobile number") String identifier) {
}
