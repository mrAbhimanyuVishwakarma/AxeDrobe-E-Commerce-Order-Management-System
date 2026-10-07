package com.ecommerce.inventory;

import com.ecommerce.inventory.event.EventItem;
import com.ecommerce.inventory.event.OrderCancelledEvent;
import com.ecommerce.inventory.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Keeps product stock in step with orders: placing an order takes stock out, cancelling puts it back.
 *
 * Each item is retried on its own (the product service may be waking up on free hosting) and the
 * event is never redelivered, so one item can't be adjusted twice when another one fails.
 */
@Slf4j
@Component
public class InventoryListener {

    private final ProductStockClient stockClient;
    private final int maxAttempts;
    private final Duration retryDelay;

    public InventoryListener(ProductStockClient stockClient,
                             @Value("${app.stock.max-attempts:8}") int maxAttempts,
                             @Value("${app.stock.retry-delay:15s}") Duration retryDelay) {
        this.stockClient = stockClient;
        this.maxAttempts = maxAttempts;
        this.retryDelay = retryDelay;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-placed}")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Order {} placed, reserving stock for {} line(s)", event.orderNumber(), event.items().size());
        for (EventItem item : event.items()) {
            adjust(event.orderNumber(), item, -item.quantity());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.order-cancelled}")
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Order {} cancelled, returning stock for {} line(s)", event.orderNumber(), event.items().size());
        for (EventItem item : event.items()) {
            adjust(event.orderNumber(), item, item.quantity());
        }
    }

    void adjust(String orderNumber, EventItem item, int delta) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                stockClient.adjust(item.productId(), delta);
                log.info("Stock {} by {} for '{}' (order {})", delta < 0 ? "reduced" : "increased",
                        Math.abs(delta), item.name(), orderNumber);
                return;
            } catch (HttpClientErrorException e) {
                // 4xx will not succeed on retry: product deleted or not enough stock left (oversold)
                log.warn("Stock change for '{}' (order {}) rejected: {}", item.name(), orderNumber, e.getStatusCode());
                return;
            } catch (RestClientException e) {
                log.warn("Product service unavailable (attempt {}/{}): {}", attempt, maxAttempts, e.getMessage());
                sleep();
            }
        }
        log.error("Gave up adjusting stock for '{}' by {} (order {})", item.name(), delta, orderNumber);
    }

    private void sleep() {
        try {
            Thread.sleep(retryDelay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
