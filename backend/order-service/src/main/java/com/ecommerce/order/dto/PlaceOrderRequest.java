package com.ecommerce.order.dto;

import com.ecommerce.order.ShippingAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaceOrderRequest(
        @NotEmpty(message = "Your bag is empty") @Size(max = 30) List<@Valid OrderLineRequest> items,
        @NotNull(message = "Add a delivery address") @Valid ShippingAddress shippingAddress) {
}
