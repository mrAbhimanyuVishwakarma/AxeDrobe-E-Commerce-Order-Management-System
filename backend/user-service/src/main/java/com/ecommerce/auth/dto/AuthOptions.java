package com.ecommerce.auth.dto;

/**
 * Tells the frontend which sign-in methods are switched on, so it only shows working options.
 */
public record AuthOptions(String googleClientId, boolean emailOtp, boolean smsOtp, boolean demoMode) {
}
