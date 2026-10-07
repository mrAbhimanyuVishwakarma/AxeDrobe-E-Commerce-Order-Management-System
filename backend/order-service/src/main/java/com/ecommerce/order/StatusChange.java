package com.ecommerce.order;

import java.time.Instant;

public record StatusChange(OrderStatus status, Instant at) {
}
