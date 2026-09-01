package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.AddCartItemRequest;
import com.ecommerce.orderservice.dto.CartResponse;
import com.ecommerce.orderservice.dto.QuantityRequest;
import com.ecommerce.orderservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(Authentication authentication) {
        return cartService.getCart(authentication.getName());
    }

    @PostMapping("/items")
    public CartResponse add(Authentication authentication,
                            @Valid @RequestBody AddCartItemRequest request) {
        return cartService.add(authentication.getName(), request.productId(), request.quantity());
    }

    @PutMapping("/items/{itemId}")
    public CartResponse update(Authentication authentication, @PathVariable Long itemId,
                               @Valid @RequestBody QuantityRequest request) {
        return cartService.update(authentication.getName(), itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse remove(Authentication authentication, @PathVariable Long itemId) {
        return cartService.remove(authentication.getName(), itemId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication authentication) {
        cartService.clear(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
