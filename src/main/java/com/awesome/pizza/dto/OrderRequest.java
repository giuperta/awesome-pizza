package com.awesome.pizza.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for receiving pizza order input.
 */
public record OrderRequest(@NotBlank(message = "Customer name is required") String customerName,
                           @NotEmpty List<String> pizzas) {}
