package com.ecommerce.productservice.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String id) { super("Review " + id + " was not found"); }
}
