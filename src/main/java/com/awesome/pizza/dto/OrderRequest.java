package com.awesome.pizza.dto;

import com.awesome.pizza.model.PizzaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for receiving pizza order input.
 */
public record OrderRequest(
        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Customer name must be at most 100 characters")
        String customerName,

        @NotEmpty(message = "At least one pizza must be selected")
        @Size(max = 20, message = "An order can contain at most 20 pizzas")
        List<@NotNull(message = "Pizza type cannot be null") PizzaType> pizzas
) {
}
