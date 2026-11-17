package com.awesome.pizza.repository;

import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PizzaOrderRepositoryTest {

    @Autowired
    private PizzaOrderRepository repository;

    @Test
    void shouldSaveAndRetrievePizzaOrder() {
        PizzaOrder order = new PizzaOrder("Giovanni", List.of(PizzaType.MARGHERITA, PizzaType.QUATTRO_FORMAGGI));
        PizzaOrder saved = repository.save(order);

        PizzaOrder retrieved = repository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getCustomerName()).isEqualTo("Giovanni");
        assertThat(retrieved.getPizzas()).containsExactly(PizzaType.MARGHERITA, PizzaType.QUATTRO_FORMAGGI);
    }

    @Test
    void shouldReturnEmptyWhenOrderNotExists() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldPaginatePendingOrdersOldestFirst() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 12, 0);
        for (int i = 0; i < 5; i++) {
            PizzaOrder order = new PizzaOrder("Customer " + i, List.of(PizzaType.MARGHERITA));
            order.setCreatedAt(base.plusMinutes(i));
            if (i == 4) {
                order.setStatus(OrderStatus.READY); // must not appear in the queue
            }
            repository.save(order);
        }

        PageRequest firstPage = PageRequest.of(0, 3, Sort.by("createdAt"));
        Page<PizzaOrder> page = repository.findByStatusIn(
                EnumSet.of(OrderStatus.RECEIVED, OrderStatus.IN_PREPARATION), firstPage);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(PizzaOrder::getCustomerName)
                .containsExactly("Customer 0", "Customer 1", "Customer 2");

        Page<PizzaOrder> secondPage = repository.findByStatusIn(
                EnumSet.of(OrderStatus.RECEIVED, OrderStatus.IN_PREPARATION), firstPage.next());
        assertThat(secondPage.getContent()).extracting(PizzaOrder::getCustomerName)
                .containsExactly("Customer 3");
        assertThat(secondPage.isLast()).isTrue();
    }
}
