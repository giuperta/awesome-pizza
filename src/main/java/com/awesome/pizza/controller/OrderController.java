package com.awesome.pizza.controller;

import com.awesome.pizza.common.Constants;
import com.awesome.pizza.dto.OrderRequest;
import com.awesome.pizza.dto.PizzaOrderResponse;
import com.awesome.pizza.exception.InvalidPizzaException;
import com.awesome.pizza.factory.PizzaOrderResponseFactory;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.service.OrderService;
import com.awesome.pizza.validation.OrderValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing pizza orders and kitchen operations.
 */
@RestController
@RequestMapping(Constants.BASE_API)
@Tag(name = "Orders", description = "Endpoints for placing and managing pizza orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create a new pizza order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/orders")
    public ResponseEntity<PizzaOrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        logger.info("Received new order request from customer '{}'", request.customerName());

        try {
            OrderValidation.validatePizzaList(request.pizzas());
        } catch (InvalidPizzaException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            throw e;
        }

        List<PizzaType> pizzaTypes = request.pizzas().stream()
                .map(p -> PizzaType.valueOf(p.toUpperCase()))
                .toList();

        PizzaOrder order = orderService.createOrder(request.customerName(), pizzaTypes);
        logger.info("Order {} created successfully", order.getId());
        return ResponseEntity.ok(PizzaOrderResponseFactory.from(order));
    }

    @Operation(summary = "Get available pizza menu")
    @ApiResponse(responseCode = "200", description = "List of available pizzas")
    @GetMapping("/menu")
    public ResponseEntity<List<String>> getMenu() {
        logger.debug("Fetching available pizza menu");
        return ResponseEntity.ok(orderService.getAvailablePizzas());
    }

    @Operation(summary = "Get an order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/orders/{id}")
    public ResponseEntity<PizzaOrderResponse> getOrder(@PathVariable @NotNull UUID id) {
        logger.debug("Fetching order with ID: {}", id);
        return orderService.getOrder(id)
                .map(PizzaOrderResponseFactory::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Order {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Get list of orders not yet completed")
    @ApiResponse(responseCode = "200", description = "List of pending orders")
    @GetMapping("/kitchen/orders")
    public List<PizzaOrderResponse> getPendingOrders() {
        logger.debug("Fetching pending kitchen orders");
        return orderService.getPendingOrders().stream()
                .map(PizzaOrderResponseFactory::from)
                .toList();
    }

    @Operation(summary = "Start preparing the next RECEIVED order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order moved to IN_PREPARATION"),
            @ApiResponse(responseCode = "204", description = "No orders available to start")
    })
    @PostMapping("/kitchen/orders/next")
    public ResponseEntity<PizzaOrderResponse> startNextOrder() {
        logger.info("Kitchen requested to start next order");
        return orderService.startNextOrder()
                .map(order -> {
                    logger.info("Order {} moved to IN_PREPARATION", order.getId());
                    return ResponseEntity.ok(PizzaOrderResponseFactory.from(order));
                })
                .orElseGet(() -> {
                    logger.info("No RECEIVED orders available to start");
                    return ResponseEntity.noContent().build();
                });
    }

    @Operation(summary = "Mark an order as READY")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order marked as READY"),
            @ApiResponse(responseCode = "400", description = "Order cannot be completed")
    })
    @PostMapping("/kitchen/orders/{id}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable @NotNull UUID id) {
        logger.info("Kitchen attempts to complete order {}", id);
        boolean success = orderService.completeOrder(id);

        if (success) {
            logger.info("Order {} marked as READY", id);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("Failed to mark order {} as READY – not in preparation", id);
            return ResponseEntity.badRequest().build();
        }
    }
}