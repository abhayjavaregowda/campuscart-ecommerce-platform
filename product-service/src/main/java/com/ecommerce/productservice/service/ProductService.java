package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.InsufficientStockException;
import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> SORT_FIELDS = Set.of("name", "price", "category", "createdAt", "stock");

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> list(String search, String category, BigDecimal minPrice,
                              BigDecimal maxPrice, Boolean inStock, int page, int size,
                              String sortBy, String direction) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        String safeSort = SORT_FIELDS.contains(sortBy) ? sortBy : "name";
        Sort.Direction safeDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(safeDirection, safeSort));

        return productRepository.findAll(buildSpecification(search, category, minPrice, maxPrice, inStock),
                pageable);
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = get(id);
        apply(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = get(id);
        productRepository.delete(product);
    }

    @Transactional
    public Product decreaseStock(Long id, int quantity) {
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (!product.isActive()) {
            throw new InsufficientStockException("Product " + id + " is not available");
        }
        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Only " + product.getStock()
                    + " units are available for product " + id);
        }
        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product increaseStock(Long id, int quantity) {
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setStock(product.getStock() + quantity);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return productRepository.findActiveCategories();
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setCategory(request.getCategory().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.isActive());
    }

    private Specification<Product> buildSpecification(String search, String category,
                                                       BigDecimal minPrice, BigDecimal maxPrice,
                                                       Boolean inStock) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isTrue(root.get("active")));

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), term),
                        builder.like(builder.lower(root.get("description")), term)
                ));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(builder.equal(builder.lower(root.get("category")),
                        category.trim().toLowerCase(Locale.ROOT)));
            }
            if (minPrice != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (Boolean.TRUE.equals(inStock)) {
                predicates.add(builder.greaterThan(root.get("stock"), 0));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
