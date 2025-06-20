package com.awesome.pizza.factory;

import com.awesome.pizza.dto.PizzaOrderResponse;
import com.awesome.pizza.model.PizzaOrder;

/**
 * Utility class to convert PizzaOrder entity into a PizzaOrderResponse DTO.
 */
public class PizzaOrderResponseFactory {

    private PizzaOrderResponseFactory() {
        // Utility class
    }

    public static PizzaOrderResponse from(PizzaOrder order) {
        return new PizzaOrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getPizzas(),
                order.getStatus()
        );
    }
}
