package com.ecommerce.event;

import com.ecommerce.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Publishes order events for the inventory and notification services.
 * A Kafka outage is logged but never fails the customer's request.
 */
@Slf4j
@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final String placedTopic;
    private final String cancelledTopic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafka,
                               @Value("${app.kafka.topics.order-placed}") String placedTopic,
                               @Value("${app.kafka.topics.order-cancelled}") String cancelledTopic) {
        this.kafka = kafka;
        this.placedTopic = placedTopic;
        this.cancelledTopic = cancelledTopic;
    }

    public void orderPlaced(Order order) {
        publish(placedTopic, order, new OrderPlacedEvent(order.getId(), order.getOrderNumber(), order.getUserId(),
                order.getCustomerName(), order.getCustomerEmail(), items(order), order.getTotal(), order.getCreatedAt()));
    }

    public void orderCancelled(Order order) {
        publish(cancelledTopic, order, new OrderCancelledEvent(order.getId(), order.getOrderNumber(),
                order.getCustomerName(), order.getCustomerEmail(), items(order), Instant.now()));
    }

    private void publish(String topic, Order order, Object event) {
        try {
            kafka.send(topic, order.getId(), event).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Failed to publish {} for order {}: {}", topic, order.getOrderNumber(), error.getMessage());
                } else {
                    log.info("Published {} for order {}", topic, order.getOrderNumber());
                }
            });
        } catch (Exception e) {
            log.error("Kafka unavailable, {} for order {} not published: {}", topic, order.getOrderNumber(), e.getMessage());
        }
    }

    private static List<EventItem> items(Order order) {
        return order.getItems().stream()
                .map(i -> new EventItem(i.productId(), i.name(), i.size(), i.quantity(), i.unitPrice()))
                .toList();
    }
}
