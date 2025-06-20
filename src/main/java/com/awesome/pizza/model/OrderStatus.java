package com.awesome.pizza.model;

/**
 * Enum representing the possible states of a pizza order.
 */
public enum OrderStatus {
    /**
     * Order has been received but not yet started.
     */
    RECEIVED,
    /**
     * Order is being prepared by the pizzaiolo.
     */
    IN_PREPARATION,
    /**
     * Order is ready for pickup/delivery.
     */
    READY
}