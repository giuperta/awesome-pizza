package com.awesome.pizza.dto;

import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaType;

import java.util.List;
import java.util.UUID;

public record PizzaOrderResponse(
        UUID id,
        String customerName,
        List<PizzaType> pizzas,
        OrderStatus status
) {}