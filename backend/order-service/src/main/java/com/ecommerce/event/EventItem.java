package com.ecommerce.event;

import java.math.BigDecimal;

public record EventItem(String productId, String name, String size, int quantity, BigDecimal unitPrice) {
}
