package com.awesome.pizza.repository;

import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PizzaOrderRepositoryTest {

    @Autowired
    private PizzaOrderRepository repository;

    @Test
    void shouldSaveAndRetrievePizzaOrder() {
        PizzaOrder order = new PizzaOrder("Giovanni", List.of(PizzaType.MARGHERITA, PizzaType.QUATTRO_FORMAGGI));
        PizzaOrder saved = repository.save(order);

        PizzaOrder retrieved = repository.findById(saved.getId()).orElseThrow();
        assertEquals(saved.getCustomerName(), retrieved.getCustomerName());
        assertEquals(saved.getPizzas(), retrieved.getPizzas());
    }

    @Test
    void shouldReturnEmptyWhenOrderNotExists() {
        UUID fakeId = UUID.randomUUID();
        assertTrue(repository.findById(fakeId).isEmpty());
    }
}