package com.awesome.pizza.service;

import com.awesome.pizza.exception.OrderCompletionException;
import com.awesome.pizza.exception.OrderNotFoundException;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.repository.PizzaOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PizzaOrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PizzaOrder result = orderService.createOrder("  Mario ", List.of(PizzaType.MARGHERITA));

        assertThat(result.getCustomerName()).isEqualTo("Mario");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldGetOrderById() {
        PizzaOrder order = new PizzaOrder("Luca", List.of(PizzaType.DIAVOLA));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThat(orderService.getOrder(order.getId())).contains(order);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldQueryPendingOrdersPaginatedOldestFirst() {
        Page<PizzaOrder> page = new PageImpl<>(List.of());
        when(orderRepository.findByStatusIn(any(), any())).thenReturn(page);

        Page<PizzaOrder> result = orderService.getPendingOrders(null, 2, 10);

        ArgumentCaptor<Collection<OrderStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findByStatusIn(statuses.capture(), pageable.capture());

        assertThat(result).isSameAs(page);
        assertThat(statuses.getValue()).containsExactlyInAnyOrder(OrderStatus.RECEIVED, OrderStatus.IN_PREPARATION);
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt"))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterPendingOrdersByStatus() {
        when(orderRepository.findByStatusIn(any(), any())).thenReturn(new PageImpl<>(List.of()));

        orderService.getPendingOrders(OrderStatus.IN_PREPARATION, 0, 20);

        ArgumentCaptor<Collection<OrderStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(orderRepository).findByStatusIn(statuses.capture(), any());
        assertThat(statuses.getValue()).containsExactly(OrderStatus.IN_PREPARATION);
    }

    @Test
    void shouldStartNextOrder() {
        PizzaOrder order = new PizzaOrder("Customer", List.of(PizzaType.MARGHERITA));
        when(orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Optional<PizzaOrder> result = orderService.startNextOrder();

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
    }

    @Test
    void shouldNotStartNextOrderIfNoneReceived() {
        when(orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED)).thenReturn(Optional.empty());

        assertThat(orderService.startNextOrder()).isEmpty();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldCompleteOrder() {
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.DIAVOLA));
        order.setStatus(OrderStatus.IN_PREPARATION);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        PizzaOrder result = orderService.completeOrder(order.getId());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void shouldThrowWhenCompletingNotInPreparation() {
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.DIAVOLA));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.completeOrder(order.getId()))
                .isInstanceOf(OrderCompletionException.class);
        verify(orderRepository, never()).save(any());
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
        assertThat(orderService.getAvailablePizzas()).contains("MARGHERITA", "DIAVOLA", "MARINARA");
    }
}
