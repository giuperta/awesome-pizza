package com.awesome.pizza.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a pizza order placed by a customer.
 * <p>
 * Uses explicit getters/setters instead of Lombok {@code @Data}: generated
 * equals/hashCode over mutable fields and lazy collections is unsafe for JPA entities.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pizza_orders")
public class PizzaOrder {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String customerName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pizza_order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "pizza", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<PizzaType> pizzas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Optimistic locking: if two pizzaioli try to take or complete the same order
     * at the same time, only one update succeeds.
     */
    @Version
    private Long version;

    public PizzaOrder(String customerName, List<PizzaType> pizzas) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.pizzas = new ArrayList<>(pizzas);
        this.status = OrderStatus.RECEIVED;
        this.createdAt = LocalDateTime.now();
    }
}
