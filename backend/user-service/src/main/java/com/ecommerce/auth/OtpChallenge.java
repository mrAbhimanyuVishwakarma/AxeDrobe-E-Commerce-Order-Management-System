package com.ecommerce.auth;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * The latest one-time code sent to an email or mobile number.
 * Mongo removes the document an hour after the last send, which also resets the send limit.
 */
@Data
@Document("otp_challenges")
public class OtpChallenge {

    @Id
    private String identifier;

    private String codeHash;

    private Instant expiresAt;

    private int attempts;

    private int sendCount;

    @Indexed(expireAfter = "1h")
    private Instant lastSentAt;
}
