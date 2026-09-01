package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    boolean existsByProductIdAndUserEmail(Long productId, String userEmail);
    Optional<Review> findByIdAndUserEmail(String id, String userEmail);
}
