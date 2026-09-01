package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserEmailOrderById(String userEmail);
    Optional<CartItem> findByIdAndUserEmail(Long id, String userEmail);
    Optional<CartItem> findByUserEmailAndProductId(String userEmail, Long productId);
    void deleteByUserEmail(String userEmail);
}
