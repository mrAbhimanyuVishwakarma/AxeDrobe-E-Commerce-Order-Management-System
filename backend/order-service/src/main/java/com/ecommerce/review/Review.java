package com.ecommerce.review;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document("reviews")
@CompoundIndex(name = "review_user_product", def = "{'userId': 1, 'productId': 1}", unique = true)
public class Review {
    @Id private String id;
    private String userId;
    private String reviewerName;
    private String orderId;
    private String productId;
    private String productName;
    private String productImageUrl;
    private int rating;
    private String comment;
    private boolean verifiedPurchase;
    private Instant createdAt;
    private Instant updatedAt;
}
