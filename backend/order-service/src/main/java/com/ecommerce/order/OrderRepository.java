package com.ecommerce.order;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
