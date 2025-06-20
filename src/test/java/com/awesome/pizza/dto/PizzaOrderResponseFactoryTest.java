package com.awesome.pizza.factory;

import com.awesome.pizza.dto.PizzaOrderResponse;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PizzaOrderResponseFactoryTest {

    @Test
    void shouldConvertPizzaOrderToResponse() {
        UUID id = UUID.randomUUID();
        String customer = "Mario Rossi";
        List<PizzaType> pizzas = List.of(PizzaType.MARGHERITA, PizzaType.DIAVOLA);

        PizzaOrder order = new PizzaOrder(customer, pizzas);
        order.setStatus(OrderStatus.READY);
        order.setId(id);

        PizzaOrderResponse response = PizzaOrderResponseFactory.from(order);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.customerName()).isEqualTo(customer);
        assertThat(response.status()).isEqualTo(OrderStatus.READY);
    }
}