package com.ecommerce.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank(message = "Shipping address is required")
        @Size(max = 500) String shippingAddress,
        @NotBlank(message = "Payment method is required")
        @Pattern(regexp = "COD|DEMO_CARD", message = "Payment method must be COD or DEMO_CARD") String paymentMethod,
        String paymentToken) {
}
