package com.ecommerce.orderservice.dto;

import jakarta.validation.constraints.Min;

public record QuantityRequest(
        @Min(value = 1, message = "Quantity must be at least one") int quantity) {
}
