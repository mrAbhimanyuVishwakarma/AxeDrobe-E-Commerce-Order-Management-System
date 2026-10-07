package com.ecommerce.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderLineRequest(
        @NotBlank String productId,
        @Size(max = 20) String size,
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 10, message = "You can order up to 10 of an item") int quantity) {
}
