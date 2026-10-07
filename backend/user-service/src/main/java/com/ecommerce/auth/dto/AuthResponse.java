package com.ecommerce.auth.dto;

import com.ecommerce.user.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
