package com.awesome.pizza.service;

import com.awesome.pizza.exception.OrderCompletionException;
import com.awesome.pizza.exception.OrderNotFoundException;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.repository.PizzaOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private PizzaOrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateOrder() {
        PizzaOrder mockOrder = new PizzaOrder("Mario", List.of(PizzaType.MARGHERITA));
        when(orderRepository.save(any())).thenReturn(mockOrder);

        PizzaOrder result = orderService.createOrder("Mario", List.of(PizzaType.MARGHERITA));

        assertThat(result).isNotNull();
        assertThat(result.getCustomerName()).isEqualTo("Mario");
        verify(orderRepository).save(any());
    }

    @Test
    void shouldGetOrderById() {
        UUID id = UUID.randomUUID();
        PizzaOrder order = new PizzaOrder("Luca", List.of(PizzaType.DIAVOLA));
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        Optional<PizzaOrder> result = orderService.getOrder(id);

        assertThat(result).isPresent();
        assertThat(result.get().getCustomerName()).isEqualTo("Luca");
    }

    @Test
    void shouldReturnPendingOrders() {
        PizzaOrder o1 = new PizzaOrder("A", List.of(PizzaType.MARGHERITA));
        o1.setStatus(OrderStatus.RECEIVED);
        PizzaOrder o2 = new PizzaOrder("B", List.of(PizzaType.DIAVOLA));
        o2.setStatus(OrderStatus.IN_PREPARATION);
        PizzaOrder o3 = new PizzaOrder("C", List.of(PizzaType.MARINARA));
        o3.setStatus(OrderStatus.READY);

        when(orderRepository.findAll()).thenReturn(List.of(o1, o2, o3));

        List<PizzaOrder> result = orderService.getPendingOrders();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldStartNextOrder() {
        PizzaOrder order = new PizzaOrder("Customer", List.of(PizzaType.MARGHERITA));
        order.setStatus(OrderStatus.RECEIVED);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        Optional<PizzaOrder> result = orderService.startNextOrder();

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
    }

    @Test
    void shouldNotStartNextOrderIfNoneReceived() {
        PizzaOrder order = new PizzaOrder("Customer", List.of(PizzaType.MARGHERITA));
        order.setStatus(OrderStatus.READY);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        Optional<PizzaOrder> result = orderService.startNextOrder();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCompleteOrder() {
        UUID id = UUID.randomUUID();
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.DIAVOLA));
        order.setStatus(OrderStatus.IN_PREPARATION);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        boolean result = orderService.completeOrder(id);

        assertThat(result).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldThrowWhenCompletingNotInPreparation() {
        UUID id = UUID.randomUUID();
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.DIAVOLA));
        order.setStatus(OrderStatus.RECEIVED);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.completeOrder(id))
                .isInstanceOf(OrderCompletionException.class);
    }

    @Test
    void shouldThrowWhenCompletingNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.completeOrder(id))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldReturnAllPizzaTypes() {
        List<String> result = orderService.getAvailablePizzas();
        assertThat(result).contains("MARGHERITA", "DIAVOLA", "MARINARA");
    }
}
