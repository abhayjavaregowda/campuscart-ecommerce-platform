package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.entity.CustomerOrder;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, String userEmail, OrderStatus status, PaymentStatus paymentStatus,
                            String paymentMethod, String shippingAddress, BigDecimal totalAmount,
                            List<OrderItemResponse> items, Instant createdAt, Instant updatedAt) {

    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getUserEmail(), order.getStatus(),
                order.getPaymentStatus(), order.getPaymentMethod(), order.getShippingAddress(),
                order.getTotalAmount(), order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt(), order.getUpdatedAt());
    }
}
