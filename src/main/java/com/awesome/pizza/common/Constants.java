package com.awesome.pizza.common;

/**
 * Central place for application-wide constants.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    public static final String BASE_API = "/api";
    public static final String ORDER_PATH = BASE_API + "/orders";
    public static final String KITCHEN_PATH = BASE_API + "/kitchen/orders";

    public static final String HEADER_ORDER_ID = "X-Order-ID";
}