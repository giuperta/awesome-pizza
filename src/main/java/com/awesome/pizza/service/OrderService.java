package com.awesome.pizza.service;

import com.awesome.pizza.exception.NoPendingOrdersException;
import com.awesome.pizza.exception.OrderCompletionException;
import com.awesome.pizza.exception.OrderNotFoundException;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.repository.PizzaOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer that encapsulates business logic related to pizza orders.
 */
@Service
public class OrderService {

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
    public PizzaOrder createOrder(String customerName, List<PizzaType> pizzas) {
        PizzaOrder order = new PizzaOrder(customerName, pizzas);
        logger.info("New order created with ID: {}", order.getId());
        return orderRepository.save(order);
    }

    /**
     * Retrieves a pizza order by its ID.
     *
     * @param id the UUID of the order
     * @return the found {@link PizzaOrder}
     * @throws OrderNotFoundException if no order is found with the given ID
     */
    public Optional<PizzaOrder> getOrder(UUID id) {
        return orderRepository.findById(id);
    }

    /**
     * Retrieves all orders that are not yet marked as READY.
     *
     * @return a list of {@link PizzaOrder} pending orders
     */
    public List<PizzaOrder> getPendingOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.READY)
                .toList();
    }

    /**
     * Starts the next RECEIVED order by setting its status to IN_PREPARATION.
     *
     * @return the updated {@link PizzaOrder}
     * @throws NoPendingOrdersException if no RECEIVED orders are available
     */
    public Optional<PizzaOrder> startNextOrder() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.RECEIVED)
                .findFirst()
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
     * @return true if the order was successfully updated
     * @throws OrderCompletionException if the order cannot be completed due to wrong state
     * @throws OrderNotFoundException if the order with the given ID does not exist
     */
    public boolean completeOrder(UUID id) {
        PizzaOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found"));

        if (order.getStatus() != OrderStatus.IN_PREPARATION) {
            throw new OrderCompletionException("Order with ID " + id + " is not in IN_PREPARATION state");
        }

        order.setStatus(OrderStatus.READY);
        orderRepository.save(order);
        logger.info("Order {} marked as READY", order.getId());
        return true;
    }

    /**
     * Returns the list of all available pizza types.
     *
     * @return a list of pizza names
     */
    public List<String> getAvailablePizzas() {
        return List.of(PizzaType.values()).stream().map(Enum::name).toList();
    }
}
