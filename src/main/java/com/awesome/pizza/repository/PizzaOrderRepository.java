package com.awesome.pizza.repository;

import com.awesome.pizza.model.PizzaOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository interface for PizzaOrder entity.
 */
public interface PizzaOrderRepository extends JpaRepository<PizzaOrder, UUID> {
}
