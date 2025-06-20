package com.awesome.pizza.exception;

/**
 * Thrown when a pizza order is not found by ID.
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}