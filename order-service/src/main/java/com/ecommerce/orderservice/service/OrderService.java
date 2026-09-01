package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.CheckoutRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.entity.CartItem;
import com.ecommerce.orderservice.entity.CustomerOrder;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import com.ecommerce.orderservice.exception.CheckoutException;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.repository.CartItemRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                        ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
    }

    @Transactional
    public OrderResponse checkout(String userEmail, CheckoutRequest request) {
        validatePayment(request);
        List<CartItem> cartItems = cartItemRepository.findByUserEmailOrderById(userEmail);
        if (cartItems.isEmpty()) {
            throw new CheckoutException("Cart is empty");
        }

        List<CartItem> reserved = new ArrayList<>();
        try {
            for (CartItem item : cartItems) {
                productClient.decreaseStock(item.getProductId(), item.getQuantity());
                reserved.add(item);
            }

            CustomerOrder order = buildOrder(userEmail, request, cartItems);
            CustomerOrder saved = orderRepository.saveAndFlush(order);
            cartItemRepository.deleteByUserEmail(userEmail);
            return OrderResponse.from(saved);
        } catch (RuntimeException exception) {
            restoreInventory(reserved);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> history(String userEmail) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(String userEmail, Long orderId) {
        return OrderResponse.from(findOwnedOrder(userEmail, orderId));
    }

    @Transactional
    public OrderResponse cancel(String userEmail, Long orderId) {
        CustomerOrder order = findOwnedOrder(userEmail, orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return OrderResponse.from(order);
        }
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new CheckoutException("A shipped or delivered order cannot be cancelled");
        }

        for (OrderItem item : order.getItems()) {
            productClient.increaseStock(item.getProductId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        return OrderResponse.from(orderRepository.save(order));
    }

    private CustomerOrder findOwnedOrder(String userEmail, Long orderId) {
        return orderRepository.findByIdAndUserEmail(orderId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " was not found"));
    }

    private CustomerOrder buildOrder(String userEmail, CheckoutRequest request, List<CartItem> cartItems) {
        CustomerOrder order = new CustomerOrder();
        order.setUserEmail(userEmail);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentMethod(request.paymentMethod());
        order.setPaymentStatus("DEMO_CARD".equals(request.paymentMethod())
                ? PaymentStatus.PAID : PaymentStatus.PENDING);
        order.setShippingAddress(request.shippingAddress().trim());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(cartItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            total = total.add(orderItem.getSubtotal());
            order.addItem(orderItem);
        }
        order.setTotalAmount(total);
        return order;
    }

    private void validatePayment(CheckoutRequest request) {
        if ("DEMO_CARD".equals(request.paymentMethod())
                && !"DEMO_SUCCESS".equals(request.paymentToken())) {
            throw new IllegalArgumentException(
                    "Demo card payment requires paymentToken DEMO_SUCCESS; never send real card details");
        }
    }

    private void restoreInventory(List<CartItem> reservedItems) {
        for (CartItem item : reservedItems) {
            try {
                productClient.increaseStock(item.getProductId(), item.getQuantity());
            } catch (RuntimeException ignored) {
                // Best effort compensation; failures remain visible in service logs in a production setup.
            }
        }
    }
}
