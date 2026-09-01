package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.CartItemResponse;
import com.ecommerce.orderservice.dto.CartResponse;
import com.ecommerce.orderservice.dto.ProductSummary;
import com.ecommerce.orderservice.entity.CartItem;
import com.ecommerce.orderservice.exception.CheckoutException;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    public CartService(CartItemRepository cartItemRepository, ProductClient productClient) {
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        return toResponse(cartItemRepository.findByUserEmailOrderById(userEmail));
    }

    @Transactional
    public CartResponse add(String userEmail, Long productId, int quantity) {
        ProductSummary product = requireAvailableProduct(productId);
        CartItem item = cartItemRepository.findByUserEmailAndProductId(userEmail, productId)
                .orElseGet(CartItem::new);
        int newQuantity = item.getId() == null ? quantity : item.getQuantity() + quantity;
        requireEnoughStock(product, newQuantity);

        item.setUserEmail(userEmail);
        item.setProductId(product.id());
        item.setProductName(product.name());
        item.setUnitPrice(product.price());
        item.setImageUrl(product.imageUrl());
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        return getCart(userEmail);
    }

    @Transactional
    public CartResponse update(String userEmail, Long cartItemId, int quantity) {
        CartItem item = findOwnedItem(userEmail, cartItemId);
        ProductSummary product = requireAvailableProduct(item.getProductId());
        requireEnoughStock(product, quantity);
        item.setQuantity(quantity);
        item.setProductName(product.name());
        item.setUnitPrice(product.price());
        item.setImageUrl(product.imageUrl());
        cartItemRepository.save(item);
        return getCart(userEmail);
    }

    @Transactional
    public CartResponse remove(String userEmail, Long cartItemId) {
        cartItemRepository.delete(findOwnedItem(userEmail, cartItemId));
        return getCart(userEmail);
    }

    @Transactional
    public void clear(String userEmail) {
        cartItemRepository.deleteByUserEmail(userEmail);
    }

    private CartItem findOwnedItem(String userEmail, Long id) {
        return cartItemRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item " + id + " was not found"));
    }

    private ProductSummary requireAvailableProduct(Long productId) {
        ProductSummary product = productClient.getProduct(productId);
        if (product == null || !product.active()) {
            throw new CheckoutException("Product " + productId + " is unavailable");
        }
        return product;
    }

    private void requireEnoughStock(ProductSummary product, int quantity) {
        if (product.stock() < quantity) {
            throw new CheckoutException("Only " + product.stock() + " units are available for product "
                    + product.id());
        }
    }

    private CartResponse toResponse(List<CartItem> items) {
        List<CartItemResponse> responses = items.stream().map(CartItemResponse::from).toList();
        BigDecimal total = responses.stream().map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(responses, total);
    }
}
