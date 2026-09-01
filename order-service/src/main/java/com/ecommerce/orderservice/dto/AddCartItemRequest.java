package com.ecommerce.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull(message = "Product id is required") Long productId,
        @Min(value = 1, message = "Quantity must be at least one") int quantity) {
}
