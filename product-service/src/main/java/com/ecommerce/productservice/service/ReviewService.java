package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ReviewListResponse;
import com.ecommerce.productservice.dto.ReviewRequest;
import com.ecommerce.productservice.dto.ReviewResponse;
import com.ecommerce.productservice.entity.Review;
import com.ecommerce.productservice.exception.DuplicateReviewException;
import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.exception.ReviewNotFoundException;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    public ReviewListResponse list(Long productId) {
        requireProduct(productId);
        List<ReviewResponse> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(ReviewResponse::from).toList();
        double average = reviews.stream().mapToInt(ReviewResponse::rating).average().orElse(0.0);
        return new ReviewListResponse(reviews, Math.round(average * 10.0) / 10.0, reviews.size());
    }

    public ReviewResponse create(Long productId, String userEmail, ReviewRequest request) {
        requireProduct(productId);
        if (reviewRepository.existsByProductIdAndUserEmail(productId, userEmail)) {
            throw new DuplicateReviewException("You have already reviewed this product");
        }
        Review review = new Review();
        review.setProductId(productId);
        review.setUserEmail(userEmail);
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
        review.setCreatedAt(Instant.now());
        return ReviewResponse.from(reviewRepository.save(review));
    }

    public void delete(String reviewId, String userEmail) {
        Review review = reviewRepository.findByIdAndUserEmail(reviewId, userEmail)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        reviewRepository.delete(review);
    }

    private void requireProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
    }
}
