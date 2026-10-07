package com.ecommerce.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document("users")
public class User {

    @Id
    private String id;

    private String name;

    // An account can be created with email, mobile or Google, so each of these is optional.
    // Sparse unique indexes keep them unique while allowing documents without the field.
    @Indexed(unique = true, sparse = true)
    private String email;

    @Indexed(unique = true, sparse = true)
    private String mobileNumber;

    @Indexed(unique = true, sparse = true)
    private String googleId;

    private String passwordHash;

    private Role role = Role.CUSTOMER;

    private Instant createdAt;

    private Instant lastLoginAt;

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }
}
