package com.example.opsdemo.order.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnSuccessfulPreview() throws Exception {
        mockMvc.perform(get("/api/orders/preview")
                        .param("productId", "1")
                        .param("quantity", "2")
                        .header("X-Trace-Id", "trace-success-001")
                        .header("X-Request-Id", "request-success-001"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "trace-success-001"))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("机械键盘"))
                .andExpect(jsonPath("$.totalAmount").value(798.00));
    }

    @Test
    void shouldReturnNotFoundForMissingProduct() throws Exception {
        mockMvc.perform(get("/api/orders/preview")
                        .param("productId", "999")
                        .header("X-Trace-Id", "trace-failure-999")
                        .header("X-Request-Id", "request-failure-999"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Trace-Id", "trace-failure-999"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found: 999"))
                .andExpect(jsonPath("$.traceId").value("trace-failure-999"))
                .andExpect(jsonPath("$.requestId").value("request-failure-999"))
                .andExpect(jsonPath("$.path").value("/api/orders/preview"));
    }
}

