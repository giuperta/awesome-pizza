package com.awesome.pizza.service;

import com.awesome.pizza.exception.OrderCompletionException;
import com.awesome.pizza.exception.OrderNotFoundException;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.repository.PizzaOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private PizzaOrderRepository repository;
    private OrderService service;

    @BeforeEach
    void setup() {
        repository = mock(PizzaOrderRepository.class);
        service = new OrderService(repository);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.DIAVOLA));
        when(repository.save(any())).thenReturn(order);

        PizzaOrder result = service.createOrder("Mario", List.of(PizzaType.DIAVOLA));

        assertNotNull(result);
        assertEquals("Mario", result.getCustomerName());
        verify(repository).save(any());
    }

    @Test
    void shouldReturnOrderById() {
        UUID id = UUID.randomUUID();
        PizzaOrder order = new PizzaOrder("Luca", List.of(PizzaType.MARGHERITA));
        when(repository.findById(id)).thenReturn(Optional.of(order));

        Optional<PizzaOrder> result = service.getOrder(id);

        assertTrue(result.isPresent());
        assertEquals("Luca", result.get().getCustomerName());
    }

    @Test
    void shouldGetPendingOrders() {
        PizzaOrder ready = new PizzaOrder("A", List.of(PizzaType.DIAVOLA));
        ready.setStatus(OrderStatus.READY);
        PizzaOrder pending = new PizzaOrder("B", List.of(PizzaType.MARGHERITA));

        when(repository.findAll()).thenReturn(List.of(ready, pending));

        List<PizzaOrder> result = service.getPendingOrders();

        assertEquals(1, result.size());
        assertEquals("B", result.get(0).getCustomerName());
    }

    @Test
    void shouldStartNextOrder() {
        PizzaOrder order = new PizzaOrder("C", List.of(PizzaType.VEGETARIANA));
        when(repository.findAll()).thenReturn(List.of(order));
        when(repository.save(any())).thenReturn(order);

        Optional<PizzaOrder> result = service.startNextOrder();

        assertTrue(result.isPresent());
        assertEquals(OrderStatus.IN_PREPARATION, result.get().getStatus());
    }

    @Test
    void shouldCompleteOrderSuccessfully() {
        UUID id = UUID.randomUUID();
        PizzaOrder order = new PizzaOrder("D", List.of(PizzaType.QUATTRO_FORMAGGI));
        order.setStatus(OrderStatus.IN_PREPARATION);
        when(repository.findById(id)).thenReturn(Optional.of(order));

        boolean result = service.completeOrder(id);

        assertTrue(result);
        verify(repository).save(order);
    }

    @Test
    void shouldThrowOrderNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.completeOrder(id));
    }

    @Test
    void shouldThrowCompletionExceptionIfWrongState() {
        UUID id = UUID.randomUUID();
        PizzaOrder order = new PizzaOrder("E", List.of(PizzaType.CAPRICCIOSA));
        order.setStatus(OrderStatus.RECEIVED);
        when(repository.findById(id)).thenReturn(Optional.of(order));

        assertThrows(OrderCompletionException.class, () -> service.completeOrder(id));
    }

    @Test
    void shouldReturnAllAvailablePizzas() {
        List<String> pizzas = service.getAvailablePizzas();
        assertTrue(pizzas.contains("MARGHERITA"));
        assertTrue(pizzas.contains("DIAVOLA"));
    }
}