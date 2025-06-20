package com.awesome.pizza.dto;

import com.awesome.pizza.model.PizzaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for receiving pizza order input.
 */
public record OrderRequest(
        @NotBlank(message = "Customer name is required") String customerName,
        @NotEmpty(message = "At least one pizza must be selected") List<PizzaType> pizzas
) {}
