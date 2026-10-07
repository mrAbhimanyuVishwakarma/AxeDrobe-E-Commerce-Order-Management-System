package com.ecommerce.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogFileTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void everyCatalogEntryIsValid() throws Exception {
        List<ProductRequest> items;
        try (InputStream in = new ClassPathResource("catalog.json").getInputStream()) {
            items = new ObjectMapper().readValue(in, new TypeReference<>() {});
        }

        assertThat(items).hasSizeGreaterThan(50);
        Set<String> images = new HashSet<>();
        for (ProductRequest item : items) {
            assertThat(validator.validate(item)).as(item.name()).isEmpty();
            assertThat(item.mrp()).as(item.name()).isGreaterThanOrEqualTo(item.price());
            // Store pricing policy: everything sells between Rs 200 and Rs 10,000
            assertThat(item.price()).as(item.name()).isBetween(BigDecimal.valueOf(200), BigDecimal.valueOf(10_000));
            assertThat(item.mrp()).as(item.name()).isLessThanOrEqualTo(BigDecimal.valueOf(10_000));
            assertThat(images.add(item.imageUrl())).as("duplicate image for " + item.name()).isTrue();
        }
    }
}
