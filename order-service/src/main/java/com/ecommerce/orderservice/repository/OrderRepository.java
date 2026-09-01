package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findByIdAndUserEmail(Long id, String userEmail);
}
