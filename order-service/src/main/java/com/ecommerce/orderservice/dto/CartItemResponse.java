package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.entity.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(Long id, Long productId, String productName, BigDecimal unitPrice,
                               int quantity, BigDecimal subtotal, String imageUrl) {

    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(item.getId(), item.getProductId(), item.getProductName(),
                item.getUnitPrice(), item.getQuantity(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())), item.getImageUrl());
    }
}
