package com.ecommerce.inventory.event;

import java.time.Instant;
import java.util.List;

public record OrderCancelledEvent(
        String orderId,
        String orderNumber,
        String customerName,
        String customerEmail,
        List<EventItem> items,
        Instant cancelledAt) {
}
