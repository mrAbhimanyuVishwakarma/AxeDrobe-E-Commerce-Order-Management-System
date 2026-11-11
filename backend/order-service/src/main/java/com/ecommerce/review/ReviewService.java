package com.ecommerce.review;

import com.ecommerce.order.Order;
import com.ecommerce.order.OrderItem;
import com.ecommerce.order.OrderRepository;
import com.ecommerce.order.OrderStatus;
import com.ecommerce.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviews;
    private final OrderRepository orders;

    public List<Review> latest(int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 20));
        return reviews.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, limit));
    }

    public Review save(AuthUser user, ReviewRequest request) {
        Order order = orders.findById(request.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUserId().equals(user.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order does not belong to you");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You can review this product after delivery");
        }
        OrderItem item = order.getItems().stream()
                .filter(candidate -> candidate.productId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "This product is not in the order"));

        Instant now = Instant.now();
        Review review = reviews.findByUserIdAndProductId(user.id(), request.productId()).orElseGet(() -> {
            Review created = new Review();
            created.setUserId(user.id());
            created.setProductId(request.productId());
            created.setCreatedAt(now);
            return created;
        });
        review.setReviewerName(user.name());
        review.setOrderId(order.getId());
        review.setProductName(item.name());
        review.setProductImageUrl(item.imageUrl());
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
        review.setVerifiedPurchase(true);
        review.setUpdatedAt(now);
        return reviews.save(review);
    }
}
