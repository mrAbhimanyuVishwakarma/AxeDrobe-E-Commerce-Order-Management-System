package com.ecommerce.inventory;

import com.ecommerce.inventory.event.EventItem;
import com.ecommerce.inventory.event.OrderCancelledEvent;
import com.ecommerce.inventory.event.OrderPlacedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class InventoryListenerTest {

    private final ProductStockClient client = mock(ProductStockClient.class);
    private final InventoryListener listener = new InventoryListener(client, 3, Duration.ZERO);

    private final EventItem tee = new EventItem("tee", "Crew Tee", "M", 2, BigDecimal.valueOf(599));
    private final EventItem cap = new EventItem("cap", "Cap", null, 1, BigDecimal.valueOf(299));

    @Test
    void placedOrderReducesStockForEveryLine() {
        listener.onOrderPlaced(new OrderPlacedEvent("o1", "AXD1", "u1", "Riya", "r@x.com", List.of(tee, cap),
                BigDecimal.TEN, Instant.now()));

        verify(client).adjust("tee", -2);
        verify(client).adjust("cap", -1);
    }

    @Test
    void cancelledOrderReturnsStock() {
        listener.onOrderCancelled(new OrderCancelledEvent("o1", "AXD1", "Riya", "r@x.com", List.of(tee), Instant.now()));

        verify(client).adjust("tee", 2);
    }

    @Test
    void retriesOnlyTheFailingItemWhenProductServiceIsDown() {
        doThrow(new ResourceAccessException("down")).doNothing().when(client).adjust("tee", -2);

        listener.onOrderPlaced(new OrderPlacedEvent("o1", "AXD1", "u1", "Riya", "r@x.com", List.of(cap, tee),
                BigDecimal.TEN, Instant.now()));

        verify(client, times(1)).adjust("cap", -1);
        verify(client, times(2)).adjust("tee", -2);
    }

    @Test
    void doesNotRetryWhenStockIsInsufficient() {
        doThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null))
                .when(client).adjust("tee", -2);

        listener.onOrderPlaced(new OrderPlacedEvent("o1", "AXD1", "u1", "Riya", "r@x.com", List.of(tee),
                BigDecimal.TEN, Instant.now()));

        verify(client, times(1)).adjust("tee", -2);
    }
}
