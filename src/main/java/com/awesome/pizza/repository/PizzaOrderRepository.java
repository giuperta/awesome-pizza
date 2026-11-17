package com.awesome.pizza.repository;

import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PizzaOrder entity.
 */
public interface PizzaOrderRepository extends JpaRepository<PizzaOrder, UUID> {

    Optional<PizzaOrder> findFirstByStatusOrderByCreatedAtAsc(OrderStatus status);

    /**
     * Orders in the given states, paginated and filtered by the database
     * (instead of loading every order in memory).
     */
    Page<PizzaOrder> findByStatusIn(Collection<OrderStatus> statuses, Pageable pageable);
}
