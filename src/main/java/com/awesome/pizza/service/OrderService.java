package com.awesome.pizza.service;

import com.awesome.pizza.exception.OrderCompletionException;
import com.awesome.pizza.exception.OrderNotFoundException;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.repository.PizzaOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service layer that encapsulates business logic related to pizza orders.
 */
@Service
public class OrderService {

    /** Orders the kitchen still has to work on. */
    public static final Set<OrderStatus> PENDING_STATUSES =
            EnumSet.of(OrderStatus.RECEIVED, OrderStatus.IN_PREPARATION);

    /** The queue is always served first-in, first-out. */
    private static final Sort QUEUE_ORDER = Sort.by(Sort.Direction.ASC, "createdAt");

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final PizzaOrderRepository orderRepository;

    public OrderService(PizzaOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Creates a new pizza order and stores it in the database.
     *
     * @param customerName the name of the customer
     * @param pizzas       the list of pizzas ordered
     * @return the created {@link PizzaOrder}
     */
    @Transactional
    public PizzaOrder createOrder(String customerName, List<PizzaType> pizzas) {
        PizzaOrder order = new PizzaOrder(customerName.trim(), pizzas);
        PizzaOrder saved = orderRepository.save(order);
        logger.info("New order created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Retrieves a pizza order by its ID.
     *
     * @param id the UUID of the order
     * @return the order, if it exists
     */
    @Transactional(readOnly = true)
    public Optional<PizzaOrder> getOrder(UUID id) {
        return orderRepository.findById(id);
    }

    /**
     * Retrieves one page of the kitchen queue, oldest orders first.
     *
     * @param status optional filter; when {@code null} all pending orders
     *               (RECEIVED and IN_PREPARATION) are returned
     * @param page   zero-based page index
     * @param size   page size
     * @return a page of {@link PizzaOrder}
     */
    @Transactional(readOnly = true)
    public Page<PizzaOrder> getPendingOrders(OrderStatus status, int page, int size) {
        Set<OrderStatus> statuses = status == null ? PENDING_STATUSES : EnumSet.of(status);
        return orderRepository.findByStatusIn(statuses, PageRequest.of(page, size, QUEUE_ORDER));
    }

    /**
     * Starts the next RECEIVED order (the oldest one) by setting its status to IN_PREPARATION.
     *
     * @return the updated order, or empty if the queue has no RECEIVED orders
     */
    @Transactional
    public Optional<PizzaOrder> startNextOrder() {
        return orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED)
                .map(order -> {
                    order.setStatus(OrderStatus.IN_PREPARATION);
                    logger.info("Order {} is now IN_PREPARATION", order.getId());
                    return orderRepository.save(order);
                });
    }

    /**
     * Marks a specific order as READY if it is currently IN_PREPARATION.
     *
     * @param id the UUID of the order to complete
     * @return the updated order
     * @throws OrderNotFoundException   if the order with the given ID does not exist
     * @throws OrderCompletionException if the order is not IN_PREPARATION
     */
    @Transactional
    public PizzaOrder completeOrder(UUID id) {
        PizzaOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found"));

        if (order.getStatus() != OrderStatus.IN_PREPARATION) {
            throw new OrderCompletionException(
                    "Order with ID " + id + " is " + order.getStatus() + ", only IN_PREPARATION orders can be completed");
        }

        order.setStatus(OrderStatus.READY);
        PizzaOrder saved = orderRepository.save(order);
        logger.info("Order {} marked as READY", id);
        return saved;
    }

    /**
     * Returns the list of all available pizza types.
     *
     * @return a list of pizza names
     */
    public List<String> getAvailablePizzas() {
        return Stream.of(PizzaType.values()).map(Enum::name).toList();
    }
}
