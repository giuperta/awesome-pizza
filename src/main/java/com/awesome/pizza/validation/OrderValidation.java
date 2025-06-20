package com.awesome.pizza.validation;

import com.awesome.pizza.exception.InvalidPizzaException;
import com.awesome.pizza.model.PizzaType;

import java.util.List;

/**
 * Validator class for checking business rules of pizza orders.
 */
public class OrderValidation {

    /**
     * Validates that all pizzas in the list are valid.
     *
     * @param pizzas list of pizza names
     * @throws InvalidPizzaException if one or more pizzas are invalid
     */
    public static void validatePizzaList(List<String> pizzas) {
        List<String> invalid = pizzas.stream()
                .filter(p -> !isValidPizza(p))
                .toList();

        if (!invalid.isEmpty()) {
            throw new InvalidPizzaException("Invalid pizzas: " + invalid);
        }
    }

    private static boolean isValidPizza(String name) {
        try {
            PizzaType.valueOf(name.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}