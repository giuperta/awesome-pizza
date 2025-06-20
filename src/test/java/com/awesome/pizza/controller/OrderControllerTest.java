package com.awesome.pizza.controller;

import com.awesome.pizza.dto.OrderRequest;
import com.awesome.pizza.dto.PizzaOrderResponse;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        OrderRequest request = new OrderRequest("Mario", List.of(PizzaType.MARGHERITA));
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.MARGHERITA));

        when(orderService.createOrder(any(), any())).thenReturn(order);

        ResponseEntity<PizzaOrderResponse> response = orderController.createOrder(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().customerName()).isEqualTo("Mario");
        assertThat(response.getBody().pizzas()).containsExactly(PizzaType.valueOf("MARGHERITA"));

        verify(orderService).createOrder("Mario", List.of(PizzaType.MARGHERITA));
    }

    @Test
    void shouldReturnMenu() {
        List<String> menu = List.of("MARGHERITA", "DIAVOLA");
        when(orderService.getAvailablePizzas()).thenReturn(menu);

        ResponseEntity<List<String>> response = orderController.getMenu();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("MARGHERITA", "DIAVOLA");
    }

    @Test
    void shouldReturnOrderById() {
        UUID id = UUID.randomUUID();
        PizzaOrder order = new PizzaOrder("Luca", List.of(PizzaType.DIAVOLA));
        order.setStatus(OrderStatus.RECEIVED);

        when(orderService.getOrder(id)).thenReturn(Optional.of(order));

        ResponseEntity<PizzaOrderResponse> response = orderController.getOrder(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().customerName()).isEqualTo("Luca");
        assertThat(response.getBody().pizzas()).contains(PizzaType.valueOf("DIAVOLA"));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() {
        UUID id = UUID.randomUUID();
        when(orderService.getOrder(id)).thenReturn(Optional.empty());

        ResponseEntity<PizzaOrderResponse> response = orderController.getOrder(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnPendingOrders() {
        List<PizzaOrder> orders = List.of(
                new PizzaOrder("Anna", List.of(PizzaType.DIAVOLA)),
                new PizzaOrder("John", List.of(PizzaType.MARGHERITA))
        );

        when(orderService.getPendingOrders()).thenReturn(orders);

        List<PizzaOrderResponse> result = orderController.getPendingOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).customerName()).isEqualTo("Anna");
        assertThat(result.get(1).customerName()).isEqualTo("John");
    }

    @Test
    void shouldStartNextOrderIfAvailable() {
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.MARGHERITA));
        when(orderService.startNextOrder()).thenReturn(Optional.of(order));

        ResponseEntity<PizzaOrderResponse> response = orderController.startNextOrder();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().customerName()).isEqualTo("Mario");
    }

    @Test
    void shouldReturn204IfNoOrderToStart() {
        when(orderService.startNextOrder()).thenReturn(Optional.empty());

        ResponseEntity<PizzaOrderResponse> response = orderController.startNextOrder();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldMarkOrderAsReady() {
        UUID id = UUID.randomUUID();
        when(orderService.completeOrder(id)).thenReturn(true);

        ResponseEntity<Void> response = orderController.completeOrder(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldFailToMarkOrderAsReady() {
        UUID id = UUID.randomUUID();
        when(orderService.completeOrder(id)).thenReturn(false);

        ResponseEntity<Void> response = orderController.completeOrder(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}