package com.awesome.pizza.exception;

/**
 * Thrown when there are no RECEIVED orders to process.
 */
public class NoPendingOrdersException extends RuntimeException {
    public NoPendingOrdersException(String message) {
        super(message);
    }
}