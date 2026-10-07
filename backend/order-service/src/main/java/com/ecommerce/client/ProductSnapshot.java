package com.ecommerce.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * The parts of a product the order service needs at checkout.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSnapshot(
        String id,
        String name,
        String brand,
        String imageUrl,
        BigDecimal price,
        int stock,
        List<String> sizes) {
}
