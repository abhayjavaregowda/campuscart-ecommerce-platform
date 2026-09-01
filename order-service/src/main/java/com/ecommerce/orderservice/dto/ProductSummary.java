package com.ecommerce.orderservice.dto;

import java.math.BigDecimal;

public record ProductSummary(Long id, String name, String description, String category,
                             BigDecimal price, int stock, String imageUrl, boolean active) {
}
