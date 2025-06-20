package com.awesome.pizza.exception;

/**
 * Thrown when an order cannot be marked as READY due to its current state.
 */
public class OrderCompletionException extends RuntimeException {
    public OrderCompletionException(String message) {
        super(message);
    }
}