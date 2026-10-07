package com.ecommerce.client;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProductClientTest {

    @ParameterizedTest
    @ValueSource(strings = {"http://product:8082", "http://product:8082/", "http://product:8082/api/products/", " http://product:8082/api/products "})
    void normalisesBaseUrl(String configured) {
        assertThat(ProductClient.normalise(configured)).isEqualTo("http://product:8082");
    }
}
