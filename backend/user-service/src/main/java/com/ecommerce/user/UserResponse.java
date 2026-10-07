package com.ecommerce.user;

import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        String email,
        String mobileNumber,
        String role,
        boolean hasPassword,
        boolean googleLinked,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole().name(),
                user.hasPassword(),
                user.getGoogleId() != null,
                user.getCreatedAt());
    }
}
