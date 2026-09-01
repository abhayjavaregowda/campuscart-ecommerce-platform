package com.ecommerce.productservice;

import com.ecommerce.productservice.dto.ReviewListResponse;
import com.ecommerce.productservice.dto.ReviewRequest;
import com.ecommerce.productservice.dto.ReviewResponse;
import com.ecommerce.productservice.entity.Review;
import com.ecommerce.productservice.exception.DuplicateReviewException;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.repository.ReviewRepository;
import com.ecommerce.productservice.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTests {

    private ReviewRepository reviewRepository;
    private ProductRepository productRepository;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        productRepository = mock(ProductRepository.class);
        reviewService = new ReviewService(reviewRepository, productRepository);
    }

    @Test
    void createsMongoReviewForExistingProduct() {
        when(productRepository.existsById(4L)).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUserEmail(4L, "student@example.com"))
                .thenReturn(false);
        when(reviewRepository.save(org.mockito.ArgumentMatchers.any(Review.class)))
                .thenAnswer(invocation -> {
                    Review review = invocation.getArgument(0);
                    review.setId("review-1");
                    return review;
                });

        ReviewResponse response = reviewService.create(4L, "student@example.com",
                new ReviewRequest(5, "Excellent product"));

        assertThat(response.id()).isEqualTo("review-1");
        assertThat(response.rating()).isEqualTo(5);
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void calculatesReviewSummary() {
        when(productRepository.existsById(4L)).thenReturn(true);
        when(reviewRepository.findByProductIdOrderByCreatedAtDesc(4L))
                .thenReturn(List.of(review("one", 4), review("two", 5), review("three", 5)));

        ReviewListResponse response = reviewService.list(4L);

        assertThat(response.reviewCount()).isEqualTo(3);
        assertThat(response.averageRating()).isEqualTo(4.7);
    }

    @Test
    void preventsSecondReviewBySameUser() {
        when(productRepository.existsById(4L)).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUserEmail(4L, "student@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(4L, "student@example.com",
                new ReviewRequest(4, "Again")))
                .isInstanceOf(DuplicateReviewException.class);
    }

    private Review review(String id, int rating) {
        Review review = new Review();
        review.setId(id);
        review.setProductId(4L);
        review.setUserEmail(id + "@example.com");
        review.setRating(rating);
        review.setComment("Review " + id);
        review.setCreatedAt(Instant.now());
        return review;
    }
}
