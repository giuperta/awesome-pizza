package com.awesome.pizza.controller;

import com.awesome.pizza.common.Constants;
import com.awesome.pizza.dto.OrderRequest;
import com.awesome.pizza.dto.PageResponse;
import com.awesome.pizza.dto.PizzaOrderResponse;
import com.awesome.pizza.factory.PizzaOrderResponseFactory;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing pizza orders and kitchen operations.
 */
@RestController
@RequestMapping(Constants.BASE_API)
@Tag(name = "Orders", description = "Endpoints for placing and managing pizza orders")
public class OrderController {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create a new pizza order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/orders")
    public ResponseEntity<PizzaOrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        logger.info("Received new order request with {} pizza(s)", request.pizzas().size());

        PizzaOrder order = orderService.createOrder(request.customerName(), request.pizzas());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(PizzaOrderResponseFactory.from(order));
    }

    @Operation(summary = "Get available pizza menu")
    @ApiResponse(responseCode = "200", description = "List of available pizzas")
    @GetMapping("/orders/menu")
    public ResponseEntity<List<String>> getMenu() {
        return ResponseEntity.ok(orderService.getAvailablePizzas());
    }

    @Operation(summary = "Get an order by ID (customers use it to track their order)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/orders/{id}")
    public ResponseEntity<PizzaOrderResponse> getOrder(@PathVariable @NotNull UUID id) {
        return orderService.getOrder(id)
                .map(PizzaOrderResponseFactory::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Order {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Kitchen queue: orders not yet completed, oldest first, paginated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One page of pending orders"),
            @ApiResponse(responseCode = "400", description = "Invalid page, size or status")
    })
    @GetMapping("/kitchen/orders")
    public PageResponse<PizzaOrderResponse> getPendingOrders(
            @Parameter(description = "Filter by status (RECEIVED or IN_PREPARATION). Default: both")
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size (1-" + MAX_PAGE_SIZE + ")")
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {

        if (status == OrderStatus.READY) {
            throw new IllegalArgumentException("The kitchen queue only contains RECEIVED and IN_PREPARATION orders");
        }
        return PageResponse.from(orderService.getPendingOrders(status, page, size), PizzaOrderResponseFactory::from);
    }

    @Operation(summary = "Start preparing the next RECEIVED order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order moved to IN_PREPARATION"),
            @ApiResponse(responseCode = "204", description = "No orders available to start"),
            @ApiResponse(responseCode = "409", description = "Order taken by another pizzaiolo at the same time, retry")
    })
    @PostMapping("/kitchen/orders/next")
    public ResponseEntity<PizzaOrderResponse> startNextOrder() {
        return orderService.startNextOrder()
                .map(order -> ResponseEntity.ok(PizzaOrderResponseFactory.from(order)))
                .orElseGet(() -> {
                    logger.info("No RECEIVED orders available to start");
                    return ResponseEntity.noContent().build();
                });
    }

    @Operation(summary = "Mark an order as READY")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order marked as READY"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order is not IN_PREPARATION")
    })
    @PostMapping("/kitchen/orders/{id}/complete")
    public ResponseEntity<PizzaOrderResponse> completeOrder(@PathVariable @NotNull UUID id) {
        return ResponseEntity.ok(PizzaOrderResponseFactory.from(orderService.completeOrder(id)));
    }
}
