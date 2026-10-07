package com.ecommerce.order;

import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.order.dto.StatusUpdateRequest;
import com.ecommerce.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order place(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(user, request);
    }

    @GetMapping
    public List<Order> myOrders(@AuthenticationPrincipal AuthUser user) {
        return orderService.ordersFor(user);
    }

    @GetMapping("/{reference}")
    public Order get(@AuthenticationPrincipal AuthUser user, @PathVariable String reference) {
        return orderService.find(user, reference);
    }

    @PostMapping("/{reference}/cancel")
    public Order cancel(@AuthenticationPrincipal AuthUser user, @PathVariable String reference) {
        return orderService.cancel(user, reference);
    }

    @GetMapping("/admin/latest")
    public List<Order> latest(@RequestParam(defaultValue = "50") int limit) {
        return orderService.latestOrders(limit);
    }

    @PatchMapping("/{reference}/status")
    public Order updateStatus(@AuthenticationPrincipal AuthUser admin, @PathVariable String reference,
                              @Valid @RequestBody StatusUpdateRequest request) {
        return orderService.updateStatus(admin, reference, request.status());
    }
}
