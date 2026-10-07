package com.ecommerce.order;

import com.ecommerce.client.ProductClient;
import com.ecommerce.client.ProductSnapshot;
import com.ecommerce.event.OrderEventPublisher;
import com.ecommerce.order.dto.OrderLineRequest;
import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    static final BigDecimal FREE_SHIPPING_FROM = BigDecimal.valueOf(999);
    static final BigDecimal SHIPPING_FEE = BigDecimal.valueOf(79);

    private static final Set<OrderStatus> CANCELLABLE = Set.of(OrderStatus.CONFIRMED);
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyMMdd").withZone(ZoneId.of("Asia/Kolkata"));
    private static final String NUMBER_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderEventPublisher events;

    public Order placeOrder(AuthUser customer, PlaceOrderRequest request) {
        // Same product and size added twice becomes one line
        Map<String, OrderLineRequest> lines = new LinkedHashMap<>();
        for (OrderLineRequest line : request.items()) {
            String key = line.productId() + "|" + (line.size() == null ? "" : line.size());
            lines.merge(key, line, (a, b) -> new OrderLineRequest(a.productId(), a.size(), a.quantity() + b.quantity()));
        }

        Map<String, ProductSnapshot> products = new LinkedHashMap<>();
        Map<String, Integer> requestedPerProduct = new LinkedHashMap<>();
        for (OrderLineRequest line : lines.values()) {
            products.computeIfAbsent(line.productId(), productClient::get);
            requestedPerProduct.merge(line.productId(), line.quantity(), Integer::sum);
        }

        requestedPerProduct.forEach((productId, quantity) -> {
            ProductSnapshot product = products.get(productId);
            if (product.stock() < quantity) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, product.stock() == 0
                        ? product.name() + " just went out of stock."
                        : "Only " + product.stock() + " left of " + product.name() + ". Please reduce the quantity.");
            }
        });

        List<OrderItem> items = new ArrayList<>();
        for (OrderLineRequest line : lines.values()) {
            ProductSnapshot product = products.get(line.productId());
            String size = resolveSize(product, line.size());
            BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(line.quantity()));
            items.add(new OrderItem(product.id(), product.name(), product.brand(), product.imageUrl(),
                    size, line.quantity(), product.price(), lineTotal));
        }

        BigDecimal subtotal = items.stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shipping = shippingFor(subtotal);

        Order order = new Order();
        order.setOrderNumber(newOrderNumber());
        order.setUserId(customer.id());
        order.setCustomerName(customer.name());
        order.setCustomerEmail(customer.email());
        order.setItems(items);
        order.setShippingAddress(request.shippingAddress());
        order.setPaymentMethod("CASH_ON_DELIVERY");
        order.setSubtotal(subtotal);
        order.setShippingFee(shipping);
        order.setTotal(subtotal.add(shipping));
        order.setCreatedAt(Instant.now());
        order.moveTo(OrderStatus.CONFIRMED);

        Order saved = orderRepository.save(order);
        events.orderPlaced(saved);
        return saved;
    }

    public List<Order> ordersFor(AuthUser customer) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(customer.id());
    }

    public List<Order> latestOrders(int limit) {
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, Math.min(limit, 200)));
    }

    /** Looks up by order number (AXD...) or database id. Customers only see their own orders. */
    public Order find(AuthUser caller, String reference) {
        String ref = reference.trim();
        Order order = (ref.toUpperCase().startsWith("AXD")
                ? orderRepository.findByOrderNumber(ref.toUpperCase())
                : orderRepository.findById(ref))
                .filter(o -> caller.isAdmin() || o.getUserId().equals(caller.id()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return order;
    }

    public Order cancel(AuthUser caller, String reference) {
        Order order = find(caller, reference);
        if (!CANCELLABLE.contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, order.getStatus() == OrderStatus.CANCELLED
                    ? "This order is already cancelled."
                    : "This order has already shipped and can no longer be cancelled.");
        }
        order.moveTo(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        events.orderCancelled(saved);
        return saved;
    }

    /** Admin only: move an order along the delivery steps. */
    public Order updateStatus(AuthUser admin, String reference, OrderStatus next) {
        Order order = find(admin, reference);
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order is closed.");
        }
        if (next == OrderStatus.CANCELLED) {
            return cancel(admin, reference);
        }
        if (next.ordinal() <= order.getStatus().ordinal()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Order is already " + order.getStatus().name().toLowerCase().replace('_', ' '));
        }
        order.moveTo(next);
        return orderRepository.save(order);
    }

    static BigDecimal shippingFor(BigDecimal subtotal) {
        return subtotal.compareTo(FREE_SHIPPING_FROM) >= 0 ? BigDecimal.ZERO : SHIPPING_FEE;
    }

    private static String resolveSize(ProductSnapshot product, String size) {
        List<String> sizes = product.sizes() == null ? List.of() : product.sizes();
        if (sizes.isEmpty()) {
            return null;
        }
        if (!StringUtils.hasText(size) || !sizes.contains(size)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose a size for " + product.name());
        }
        return size;
    }

    static String newOrderNumber() {
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            suffix.append(NUMBER_CHARS.charAt(RANDOM.nextInt(NUMBER_CHARS.length())));
        }
        return "AXD" + NUMBER_DATE.format(Instant.now()) + "-" + suffix;
    }
}
