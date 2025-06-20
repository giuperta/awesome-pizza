package com.awesome.pizza.validation;

import com.awesome.pizza.exception.InvalidPizzaException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderValidationTest {

    @Test
    void shouldNotThrowWhenPizzaNamesAreValid() {
        List<String> valid = List.of("MARGHERITA", "DIAVOLA", "CAPRICCIOSA");
        assertDoesNotThrow(() -> OrderValidation.validatePizzaList(valid));
    }

    @Test
    void shouldThrowWhenAtLeastOnePizzaIsInvalid() {
        List<String> mixed = List.of("MARGHERITA", "INVALIDO");
        InvalidPizzaException ex = assertThrows(InvalidPizzaException.class, () -> OrderValidation.validatePizzaList(mixed));
        assertTrue(ex.getMessage().contains("INVALIDO"));
    }

    @Test
    void shouldThrowWhenAllPizzasAreInvalid() {
        List<String> invalid = List.of("ANANAS", "NUTELLA");
        InvalidPizzaException ex = assertThrows(InvalidPizzaException.class, () -> OrderValidation.validatePizzaList(invalid));
        assertTrue(ex.getMessage().contains("ANANAS"));
        assertTrue(ex.getMessage().contains("NUTELLA"));
    }

    @Test
    void shouldHandleEmptyListWithoutException() {
        assertDoesNotThrow(() -> OrderValidation.validatePizzaList(List.of()));
    }
}