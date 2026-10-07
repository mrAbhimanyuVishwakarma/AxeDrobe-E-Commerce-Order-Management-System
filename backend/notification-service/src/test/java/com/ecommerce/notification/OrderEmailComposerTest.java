package com.ecommerce.notification;

import com.ecommerce.notification.OrderEmailComposer.Email;
import com.ecommerce.notification.event.EventItem;
import com.ecommerce.notification.event.OrderPlacedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEmailComposerTest {

    private final OrderEmailComposer composer = new OrderEmailComposer("AxeDrobe", "https://shop.example/");

    @Test
    void confirmationListsItemsAndTotal() {
        Email email = composer.orderPlaced(new OrderPlacedEvent("o1", "AXD261006-ABCDE", "u1", "Riya Sharma",
                "riya@example.com",
                List.of(new EventItem("p1", "Linen <Shirt>", "M", 2, BigDecimal.valueOf(1299))),
                BigDecimal.valueOf(2598), Instant.now()));

        assertThat(email.subject()).isEqualTo("Your AxeDrobe order AXD261006-ABCDE is confirmed");
        assertThat(email.html())
                .contains("Hi Riya,")
                .contains("Linen &lt;Shirt&gt;")
                .contains("x2")
                .contains("https://shop.example/orders");
    }

    @Test
    void formatsRupeesWithoutDecimals() {
        assertThat(OrderEmailComposer.rupees(BigDecimal.valueOf(124999))).isEqualTo("₹1,24,999");
    }
}
