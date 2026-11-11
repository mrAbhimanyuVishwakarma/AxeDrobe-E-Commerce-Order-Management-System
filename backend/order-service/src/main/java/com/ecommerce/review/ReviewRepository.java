package com.ecommerce.review;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    Optional<Review> findByUserIdAndProductId(String userId, String productId);
    List<Review> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
