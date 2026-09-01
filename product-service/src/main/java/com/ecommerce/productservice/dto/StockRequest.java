package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.Min;

public class StockRequest {

    @Min(value = 1, message = "Quantity must be at least one")
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
