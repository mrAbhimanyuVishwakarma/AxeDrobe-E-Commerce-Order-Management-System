package com.ecommerce.inventory;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.SecretKey;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Calls the product service's stock endpoint. Authenticates with a short-lived token signed with
 * the shared JWT secret and carrying the ADMIN role, the same way a signed-in admin would.
 */
@Component
public class ProductStockClient {

    private final RestClient http;
    private final SecretKey key;

    public ProductStockClient(RestClient.Builder builder,
                              @Value("${app.product-service-url}") String baseUrl,
                              @Value("${app.jwt.secret}") String secret) {
        // JDK HttpClient: unlike HttpURLConnection it supports PATCH
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.http = builder.baseUrl(normalise(baseUrl)).requestFactory(requestFactory).build();
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public void adjust(String productId, int delta) {
        http.patch()
                .uri("/api/products/{id}/stock", productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken())
                .body(Map.of("delta", delta))
                .retrieve()
                .toBodilessEntity();
    }

    private String serviceToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("inventory-service")
                .claim("name", "Inventory Service")
                .claim("role", "ADMIN")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(5))))
                .signWith(key)
                .compact();
    }

    static String normalise(String url) {
        String base = url.trim().replaceAll("/+$", "");
        return base.endsWith("/api/products") ? base.substring(0, base.length() - "/api/products".length()) : base;
    }
}
