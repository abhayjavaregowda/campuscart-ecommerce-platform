package com.ecommerce.productservice.dto;

import com.ecommerce.productservice.entity.Review;

import java.time.Instant;

public record ReviewResponse(String id, Long productId, String userEmail, int rating,
                             String comment, Instant createdAt) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(review.getId(), review.getProductId(), review.getUserEmail(),
                review.getRating(), review.getComment(), review.getCreatedAt());
    }
}
