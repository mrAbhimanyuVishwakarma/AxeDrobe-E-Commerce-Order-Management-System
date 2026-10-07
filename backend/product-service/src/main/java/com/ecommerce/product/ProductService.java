package com.ecommerce.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongo;

    public List<Product> findAll() {
        return productRepository.findAll(Sort.by("createdAt"));
    }

    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        product.setCreatedAt(Instant.now());
        return productRepository.save(product);
    }

    public Product update(String id, ProductRequest request) {
        Product product = findById(id);
        apply(product, request);
        product.setUpdatedAt(Instant.now());
        return productRepository.save(product);
    }

    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
    }

    /**
     * Adds or removes stock in a single atomic update. Removing more than is available fails
     * instead of going negative, so two orders can never both take the last item.
     */
    public Product adjustStock(String id, int delta) {
        Criteria criteria = Criteria.where("_id").is(id);
        if (delta < 0) {
            criteria = criteria.and("stock").gte(-delta);
        }
        Update update = new Update().inc("stock", delta).set("updatedAt", Instant.now());
        Product updated = mongo.findAndModify(Query.query(criteria), update,
                FindAndModifyOptions.options().returnNew(true), Product.class);
        if (updated == null) {
            throw productRepository.existsById(id)
                    ? new ResponseStatusException(HttpStatus.CONFLICT, "Not enough stock")
                    : new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return updated;
    }

    static void apply(Product product, ProductRequest request) {
        if (request.mrp().compareTo(request.price()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MRP cannot be lower than the selling price");
        }
        product.setName(request.name().trim());
        product.setBrand(request.brand().trim());
        product.setCategory(request.category().trim());
        product.setGender(request.gender());
        product.setColor(request.color());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setMrp(request.mrp());
        product.setStock(request.stock());
        product.setRating(request.rating() != null ? request.rating() : 0);
        product.setRatingCount(request.ratingCount() != null ? request.ratingCount() : 0);
        product.setSizes(request.sizes() != null ? request.sizes() : List.of());
        product.setImageUrl(request.imageUrl());
    }
}
