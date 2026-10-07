package com.ecommerce.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @param demoCode only present when the server runs in demo mode
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OtpResponse(String channel, String sentTo, boolean newUser, int resendAfterSeconds, String demoCode) {
}
