package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.CheckoutRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(Authentication authentication,
                                                   @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(authentication.getName(), request));
    }

    @GetMapping
    public List<OrderResponse> history(Authentication authentication) {
        return orderService.history(authentication.getName());
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(Authentication authentication, @PathVariable Long orderId) {
        return orderService.get(authentication.getName(), orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(Authentication authentication, @PathVariable Long orderId) {
        return orderService.cancel(authentication.getName(), orderId);
    }
}
