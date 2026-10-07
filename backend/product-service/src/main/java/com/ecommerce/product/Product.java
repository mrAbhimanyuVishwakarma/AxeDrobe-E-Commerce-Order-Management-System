package com.ecommerce.product;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@Document("products")
public class Product {

    @Id
    private String id;

    private String name;

    private String brand;

    @Indexed
    private String category;

    private Gender gender;

    private String color;

    private String description;

    /** Selling price in INR */
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    /** Maximum retail price in INR, shown struck through when higher than the price */
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal mrp;

    private int stock;

    private double rating;

    private int ratingCount;

    private List<String> sizes;

    private String imageUrl;

    private Instant createdAt;

    private Instant updatedAt;
}
