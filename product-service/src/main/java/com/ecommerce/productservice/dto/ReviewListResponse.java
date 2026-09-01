package com.ecommerce.productservice.dto;

import java.util.List;

public record ReviewListResponse(List<ReviewResponse> reviews, double averageRating, int reviewCount) {
}
