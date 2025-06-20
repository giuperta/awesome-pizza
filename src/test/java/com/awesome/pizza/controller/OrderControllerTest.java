package com.awesome.pizza.controller;

import com.awesome.pizza.dto.OrderRequest;
import com.awesome.pizza.model.OrderStatus;
import com.awesome.pizza.model.PizzaOrder;
import com.awesome.pizza.model.PizzaType;
import com.awesome.pizza.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(OrderControllerTest.MockConfig.class)
class OrderControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {
        OrderRequest req = new OrderRequest("Test", List.of("MARGHERITA"));
        PizzaOrder order = new PizzaOrder("Test", List.of(PizzaType.MARGHERITA));
        Mockito.when(orderService.createOrder(any(), any())).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectEmptyPizzaList() throws Exception {
        OrderRequest req = new OrderRequest("Test", List.of());
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        UUID id = UUID.randomUUID();
        PizzaOrder order = new PizzaOrder("Customer", List.of(PizzaType.DIAVOLA));
        order.setStatus(OrderStatus.RECEIVED);
        Mockito.when(orderService.getOrder(eq(id))).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundForMissingOrder() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(orderService.getOrder(eq(id))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnMenu() throws Exception {
        Mockito.when(orderService.getAvailablePizzas()).thenReturn(List.of("MARGHERITA", "DIAVOLA"));

        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("MARGHERITA"));
    }
}