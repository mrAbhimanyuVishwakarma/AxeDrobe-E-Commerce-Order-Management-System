package com.ecommerce.security;

/**
 * The authenticated caller, rebuilt from the JWT on every request.
 */
public record AuthUser(String id, String name, String email, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
