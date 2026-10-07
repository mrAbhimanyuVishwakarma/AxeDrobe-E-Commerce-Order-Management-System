package com.ecommerce.product;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceTest {

    private static ProductRequest request(int price, int mrp) {
        return new ProductRequest(" Linen Shirt ", "Urban Loom", "Shirts", Gender.MEN, "White", "desc",
                BigDecimal.valueOf(price), BigDecimal.valueOf(mrp), 10, null, null, null, "https://img");
    }

    @Test
    void applyCopiesFieldsAndFillsDefaults() {
        Product product = new Product();
        ProductService.apply(product, request(999, 1499));

        assertThat(product.getName()).isEqualTo("Linen Shirt");
        assertThat(product.getRating()).isZero();
        assertThat(product.getSizes()).isEqualTo(List.of());
    }

    @Test
    void rejectsMrpBelowPrice() {
        assertThatThrownBy(() -> ProductService.apply(new Product(), request(1499, 999)))
                .isInstanceOf(ResponseStatusException.class);
    }
}
