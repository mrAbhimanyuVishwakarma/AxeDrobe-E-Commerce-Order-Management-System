package com.ecommerce.notification.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderPlacedEvent(
        String orderId,
        String orderNumber,
        String userId,
        String customerName,
        String customerEmail,
        List<EventItem> items,
        BigDecimal total,
        Instant placedAt) {
}
