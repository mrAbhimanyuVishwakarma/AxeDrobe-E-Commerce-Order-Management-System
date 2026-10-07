package com.ecommerce.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the starter catalog (src/main/resources/catalog.json) into an empty database.
 * Disable with CATALOG_SEED=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.catalog.seed", havingValue = "true", matchIfMissing = true)
public class CatalogSeeder implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (productRepository.count() > 0) {
            return;
        }
        List<ProductRequest> items;
        try (InputStream in = new ClassPathResource("catalog.json").getInputStream()) {
            items = objectMapper.readValue(in, new TypeReference<>() {});
        }

        // Stagger creation times so "New arrivals" has a stable order: later entries are newer
        Instant start = Instant.now().minus(Duration.ofHours(6L * items.size()));
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Product product = new Product();
            ProductService.apply(product, items.get(i));
            product.setCreatedAt(start.plus(Duration.ofHours(6L * i)));
            products.add(product);
        }
        productRepository.saveAll(products);
        log.info("Seeded catalog with {} products", products.size());
    }
}
