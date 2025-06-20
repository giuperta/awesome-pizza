package com.awesome.pizza.exception;

/**
 * Exception thrown when an invalid pizza name is used in the request.
 */
public class InvalidPizzaException extends RuntimeException {
    public InvalidPizzaException(String message) {
        super(message);
    }
}
