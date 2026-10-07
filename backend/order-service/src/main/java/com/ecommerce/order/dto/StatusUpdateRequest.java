package com.ecommerce.order.dto;

import com.ecommerce.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull OrderStatus status) {
}
