package com.ecommerce.order;

import com.ecommerce.client.ProductClient;
import com.ecommerce.client.ProductSnapshot;
import com.ecommerce.event.OrderEventPublisher;
import com.ecommerce.order.dto.OrderLineRequest;
import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderRepository repository = mock(OrderRepository.class);
    private final ProductClient products = mock(ProductClient.class);
    private final OrderEventPublisher events = mock(OrderEventPublisher.class);
    private final OrderService service = new OrderService(repository, products, events);

    private final AuthUser riya = new AuthUser("u-1", "Riya", "riya@example.com", "CUSTOMER");
    private final ShippingAddress address = new ShippingAddress("Riya Sharma", "9876543210", "12 MG Road", null,
            "Bengaluru", "Karnataka", "560001");

    @BeforeEach
    void setUp() {
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(products.get("tee")).thenReturn(new ProductSnapshot("tee", "Crew Tee", "Axe Basics", "img",
                BigDecimal.valueOf(599), 5, List.of("S", "M", "L")));
        when(products.get("cap")).thenReturn(new ProductSnapshot("cap", "Cap", "Axe Basics", "img",
                BigDecimal.valueOf(299), 50, List.of()));
    }

    @Test
    void placesOrderWithTotalsAndPublishesEvent() {
        Order order = service.placeOrder(riya, new PlaceOrderRequest(List.of(
                new OrderLineRequest("tee", "M", 1),
                new OrderLineRequest("tee", "M", 1),
                new OrderLineRequest("cap", null, 1)), address));

        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems().get(0).quantity()).isEqualTo(2);
        assertThat(order.getSubtotal()).isEqualByComparingTo("1497");
        assertThat(order.getShippingFee()).isEqualByComparingTo("0");
        assertThat(order.getTotal()).isEqualByComparingTo("1497");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getOrderNumber()).matches("AXD\\d{6}-[A-Z2-9]{5}");
        assertThat(order.getUserId()).isEqualTo("u-1");
        verify(events).orderPlaced(order);
    }

    @Test
    void chargesShippingBelowThreshold() {
        Order order = service.placeOrder(riya, new PlaceOrderRequest(List.of(new OrderLineRequest("cap", null, 1)), address));
        assertThat(order.getTotal()).isEqualByComparingTo("378");
    }

    @Test
    void rejectsMoreThanAvailableStockAcrossSizes() {
        assertThatThrownBy(() -> service.placeOrder(riya, new PlaceOrderRequest(List.of(
                new OrderLineRequest("tee", "S", 3),
                new OrderLineRequest("tee", "L", 3)), address)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only 5 left");
        verify(events, never()).orderPlaced(any());
    }

    @Test
    void requiresValidSizeForSizedProducts() {
        assertThatThrownBy(() -> service.placeOrder(riya, new PlaceOrderRequest(List.of(new OrderLineRequest("tee", "XXL", 1)), address)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("choose a size");
    }

    @Test
    void customersCannotSeeOtherCustomersOrders() {
        Order other = new Order();
        other.setUserId("someone-else");
        when(repository.findByOrderNumber("AXD261006-ABCDE")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.find(riya, "axd261006-abcde"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void onlyConfirmedOrdersCanBeCancelled() {
        Order shipped = new Order();
        shipped.setUserId("u-1");
        shipped.moveTo(OrderStatus.CONFIRMED);
        shipped.moveTo(OrderStatus.SHIPPED);
        when(repository.findById("o-1")).thenReturn(Optional.of(shipped));

        assertThatThrownBy(() -> service.cancel(riya, "o-1")).isInstanceOf(ResponseStatusException.class);
        verify(events, never()).orderCancelled(any());
    }
}
