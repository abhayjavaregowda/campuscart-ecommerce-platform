package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.ProductSummary;
import com.ecommerce.orderservice.exception.CheckoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(RestClient.Builder builder, @Value("${product-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public ProductSummary getProduct(Long productId) {
        try {
            return restClient.get().uri("/api/products/{id}", productId)
                    .retrieve().body(ProductSummary.class);
        } catch (RestClientResponseException exception) {
            throw new CheckoutException("Product " + productId + " is unavailable");
        }
    }

    public void decreaseStock(Long productId, int quantity) {
        changeStock(productId, quantity, "decrease");
    }

    public void increaseStock(Long productId, int quantity) {
        changeStock(productId, quantity, "increase");
    }

    private void changeStock(Long productId, int quantity, String operation) {
        try {
            restClient.post().uri("/api/products/{id}/stock/{operation}", productId, operation)
                    .body(Map.of("quantity", quantity)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new CheckoutException("Inventory update failed for product " + productId);
        }
    }
}
