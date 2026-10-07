package com.ecommerce.order;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

/**
 * A line in an order. Name, image and price are copied from the catalog at checkout
 * so the order still reads correctly if the product changes later.
 */
public record OrderItem(
        String productId,
        String name,
        String brand,
        String imageUrl,
        String size,
        int quantity,
        @Field(targetType = FieldType.DECIMAL128) BigDecimal unitPrice,
        @Field(targetType = FieldType.DECIMAL128) BigDecimal lineTotal) {
}
