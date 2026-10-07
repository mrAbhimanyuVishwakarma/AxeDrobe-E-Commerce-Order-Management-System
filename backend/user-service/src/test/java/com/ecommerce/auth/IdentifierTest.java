package com.ecommerce.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentifierTest {

    @Test
    void normalisesEmail() {
        Identifier id = Identifier.parse("  Riya.Sharma@Gmail.com ");
        assertThat(id.type()).isEqualTo(Identifier.Type.EMAIL);
        assertThat(id.value()).isEqualTo("riya.sharma@gmail.com");
        assertThat(id.masked()).isEqualTo("ri****@gmail.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"9876543210", "+91 98765 43210", "919876543210", "09876543210", "98765-43210"})
    void normalisesIndianMobileNumbers(String raw) {
        Identifier id = Identifier.parse(raw);
        assertThat(id.type()).isEqualTo(Identifier.Type.MOBILE);
        assertThat(id.value()).isEqualTo("9876543210");
        assertThat(id.masked()).isEqualTo("+91 ******3210");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "12345", "5876543210", "user@", "user@site"})
    void rejectsInvalidInput(String raw) {
        assertThatThrownBy(() -> Identifier.parse(raw)).isInstanceOf(ResponseStatusException.class);
    }
}
