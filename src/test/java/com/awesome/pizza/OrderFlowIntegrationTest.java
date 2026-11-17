package com.awesome.pizza;

import com.awesome.pizza.repository.PizzaOrderRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flow through the real application (controller, service, JPA, H2):
 * customers order, the pizzaiolo works the paginated queue first-in first-out.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowIntegrationTest {

    private static final String API = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PizzaOrderRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private String placeOrder(String customer, String pizza) throws Exception {
        String body = mockMvc.perform(post(API + "/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"" + customer + "\",\"pizzas\":[\"" + pizza + "\"]}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void fullOrderLifecycleWithPaginatedQueue() throws Exception {
        String first = placeOrder("Anna", "MARGHERITA");
        Thread.sleep(5); // distinct creation timestamps for a deterministic FIFO order
        placeOrder("Bruno", "DIAVOLA");
        Thread.sleep(5);
        placeOrder("Carla", "MARINARA");

        // Queue, page 1 of 2
        mockMvc.perform(get(API + "/kitchen/orders").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].customerName").value("Anna"))
                .andExpect(jsonPath("$.content[1].customerName").value("Bruno"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));

        // Queue, page 2 of 2
        mockMvc.perform(get(API + "/kitchen/orders").param("size", "2").param("page", "1"))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].customerName").value("Carla"))
                .andExpect(jsonPath("$.last").value(true));

        // The pizzaiolo takes the oldest order
        mockMvc.perform(post(API + "/kitchen/orders/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(first))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        mockMvc.perform(get(API + "/kitchen/orders").param("status", "IN_PREPARATION"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(first));

        // ...completes it, and the customer sees it READY
        mockMvc.perform(post(API + "/kitchen/orders/" + first + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        mockMvc.perform(get(API + "/orders/" + first))
                .andExpect(jsonPath("$.status").value("READY"));

        // READY orders leave the queue; completing twice is a conflict
        mockMvc.perform(get(API + "/kitchen/orders"))
                .andExpect(jsonPath("$.totalElements").value(2));
        mockMvc.perform(post(API + "/kitchen/orders/" + first + "/complete"))
                .andExpect(status().isConflict());
    }

    @Test
    void emptyQueue() throws Exception {
        mockMvc.perform(post(API + "/kitchen/orders/next"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(API + "/kitchen/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void unknownPathKeepsItsStatus() throws Exception {
        mockMvc.perform(get(API + "/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
