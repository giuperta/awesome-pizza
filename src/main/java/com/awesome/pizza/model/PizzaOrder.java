package com.awesome.pizza.model;

import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a pizza order placed by a customer.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "pizza_orders")
public class PizzaOrder {

    @Id
    private UUID id;

    private String customerName;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<PizzaType> pizzas;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;

    public PizzaOrder(String customerName, List<PizzaType> pizzas) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.pizzas = pizzas;
        this.status = OrderStatus.RECEIVED;
        this.createdAt = LocalDateTime.now();
    }
}