package com.awesome.pizza.exception;

import com.awesome.pizza.controller.OrderController;
import com.awesome.pizza.service.OrderService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({GlobalExceptionHandlerTest.MockConfig.class, GlobalExceptionHandlerTest.ValidatorConfig.class})
class GlobalExceptionHandlerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        public OrderService orderService() {
            OrderService mock = Mockito.mock(OrderService.class);
            Mockito.when(mock.getOrder(any())).thenReturn(Optional.empty());
            return mock;
        }
    }

    @TestConfiguration
    static class ValidatorConfig {
        @Bean
        public Validator validator() {
            return jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnNotFoundWhenOrderMissing() throws Exception {
        mockMvc.perform(get("/api/orders/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}