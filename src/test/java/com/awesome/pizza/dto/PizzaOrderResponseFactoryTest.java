package com.awesome.pizza.dto;

import com.awesome.pizza.factory.PizzaOrderResponseFactory;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PizzaOrderResponseFactoryTest {

    @Test
    void shouldCorrectlyMapEntityToDto() {
        PizzaOrder order = new PizzaOrder("Carlo", List.of(PizzaType.DIAVOLA, PizzaType.VEGETARIANA));
        order.setStatus(OrderStatus.IN_PREPARATION);

        PizzaOrderResponse response = PizzaOrderResponseFactory.from(order);

        assertEquals(order.getId(), response.id());
        assertEquals(order.getCustomerName(), response.customerName());
        assertEquals(order.getStatus(), response.status());
        assertEquals(order.getPizzas(), response.pizzas());
    }
}