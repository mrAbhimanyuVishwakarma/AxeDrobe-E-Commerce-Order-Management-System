package com.ecommerce.order;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document("orders")
public class Order {

    @Id
    private String id;

    /** Customer facing reference, e.g. AXD261006-7KQ2M */
    @Indexed(unique = true)
    private String orderNumber;

    @Indexed
    private String userId;

    private String customerName;

    private String customerEmail;

    private List<OrderItem> items = new ArrayList<>();

    private ShippingAddress shippingAddress;

    private String paymentMethod;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal subtotal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal shippingFee;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal total;

    private OrderStatus status;

    private List<StatusChange> statusHistory = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;

    public void moveTo(OrderStatus next) {
        Instant now = Instant.now();
        status = next;
        statusHistory.add(new StatusChange(next, now));
        updatedAt = now;
    }

    public int itemCount() {
        return items.stream().mapToInt(OrderItem::quantity).sum();
    }
}
