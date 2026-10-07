package com.ecommerce.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Slf4j
@Component
public class ProductClient {

    private final RestClient http;

    public ProductClient(RestClient.Builder builder, @Value("${app.product-service-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        // Generous read timeout: on free hosting the product service may be waking up
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.http = builder.baseUrl(normalise(baseUrl)).requestFactory(requestFactory).build();
    }

    public ProductSnapshot get(String productId) {
        try {
            return http.get().uri("/api/products/{id}", productId).retrieve().body(ProductSnapshot.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "An item in your bag is no longer available. Please remove it and try again.");
        } catch (RestClientException e) {
            log.error("Product service call failed for {}: {}", productId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "We couldn't check stock right now. Please try again in a moment.");
        }
    }

    /** Accepts "http://host:8082", "http://host:8082/" or the older "http://host:8082/api/products/". */
    static String normalise(String url) {
        String base = url.trim().replaceAll("/+$", "");
        return base.endsWith("/api/products") ? base.substring(0, base.length() - "/api/products".length()) : base;
    }
}
