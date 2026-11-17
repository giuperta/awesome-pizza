package com.awesome.pizza.controller;

import com.awesome.pizza.exception.OrderCompletionException;
import com.awesome.pizza.exception.OrderNotFoundException;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private static final String API = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndReturnLocation() throws Exception {
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.MARGHERITA));
        when(orderService.createOrder(any(), any())).thenReturn(order);

        mockMvc.perform(post(API + "/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Mario\",\"pizzas\":[\"MARGHERITA\"]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/orders/" + order.getId())))
                .andExpect(jsonPath("$.customerName").value("Mario"))
                .andExpect(jsonPath("$.pizzas[0]").value("MARGHERITA"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        verify(orderService).createOrder("Mario", List.of(PizzaType.MARGHERITA));
    }

    @Test
    void shouldRejectInvalidOrder() throws Exception {
        mockMvc.perform(post(API + "/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\" \",\"pizzas\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.length()").value(2));

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectUnknownPizzaWith400() throws Exception {
        mockMvc.perform(post(API + "/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Mario\",\"pizzas\":[\"ANANAS\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(containsString("MARGHERITA")));
    }

    @Test
    void shouldReturnMenu() throws Exception {
        when(orderService.getAvailablePizzas()).thenReturn(List.of("MARGHERITA", "DIAVOLA"));

        mockMvc.perform(get(API + "/orders/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("MARGHERITA"))
                .andExpect(jsonPath("$[1]").value("DIAVOLA"));
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        PizzaOrder order = new PizzaOrder("Luca", List.of(PizzaType.DIAVOLA));
        when(orderService.getOrder(order.getId())).thenReturn(Optional.of(order));

        mockMvc.perform(get(API + "/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Luca"));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.getOrder(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(API + "/orders/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400ForMalformedId() throws Exception {
        mockMvc.perform(get(API + "/orders/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }

    @Test
    void shouldReturnPaginatedKitchenQueue() throws Exception {
        List<PizzaOrder> orders = List.of(
                new PizzaOrder("Anna", List.of(PizzaType.DIAVOLA)),
                new PizzaOrder("John", List.of(PizzaType.MARGHERITA)));
        when(orderService.getPendingOrders(isNull(), eq(1), eq(2)))
                .thenReturn(new PageImpl<>(orders, PageRequest.of(1, 2), 5));

        mockMvc.perform(get(API + "/kitchen/orders").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].customerName").value("Anna"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldUseDefaultPaginationAndStatusFilter() throws Exception {
        when(orderService.getPendingOrders(OrderStatus.RECEIVED, 0, OrderController.DEFAULT_PAGE_SIZE))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, OrderController.DEFAULT_PAGE_SIZE), 0));

        mockMvc.perform(get(API + "/kitchen/orders").param("status", "RECEIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldRejectInvalidPaginationParameters() throws Exception {
        mockMvc.perform(get(API + "/kitchen/orders").param("size", "500"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(API + "/kitchen/orders").param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(API + "/kitchen/orders").param("status", "READY"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(API + "/kitchen/orders").param("status", "COOKING"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldStartNextOrderIfAvailable() throws Exception {
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.MARGHERITA));
        order.setStatus(OrderStatus.IN_PREPARATION);
        when(orderService.startNextOrder()).thenReturn(Optional.of(order));

        mockMvc.perform(post(API + "/kitchen/orders/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));
    }

    @Test
    void shouldReturn204IfNoOrderToStart() throws Exception {
        when(orderService.startNextOrder()).thenReturn(Optional.empty());

        mockMvc.perform(post(API + "/kitchen/orders/next"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldMarkOrderAsReady() throws Exception {
        PizzaOrder order = new PizzaOrder("Mario", List.of(PizzaType.MARGHERITA));
        order.setStatus(OrderStatus.READY);
        when(orderService.completeOrder(order.getId())).thenReturn(order);

        mockMvc.perform(post(API + "/kitchen/orders/" + order.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void shouldReturn409WhenOrderCannotBeCompleted() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.completeOrder(id)).thenThrow(new OrderCompletionException("not in preparation"));

        mockMvc.perform(post(API + "/kitchen/orders/" + id + "/complete"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenCompletingUnknownOrder() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.completeOrder(id)).thenThrow(new OrderNotFoundException("not found"));

        mockMvc.perform(post(API + "/kitchen/orders/" + id + "/complete"))
                .andExpect(status().isNotFound());
    }
}
