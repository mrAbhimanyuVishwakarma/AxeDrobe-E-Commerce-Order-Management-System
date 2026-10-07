package com.ecommerce.product;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Body for creating or updating a product. Also the shape of each entry in catalog.json.
 */
public record ProductRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 60) String brand,
        @NotBlank @Size(max = 60) String category,
        @NotNull Gender gender,
        @Size(max = 40) String color,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("1") BigDecimal price,
        @NotNull @DecimalMin("1") BigDecimal mrp,
        @Min(0) int stock,
        @DecimalMin("0") @DecimalMax("5") Double rating,
        @Min(0) Integer ratingCount,
        List<@NotBlank String> sizes,
        @NotBlank String imageUrl) {
}
