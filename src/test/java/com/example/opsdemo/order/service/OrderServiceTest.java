package com.example.opsdemo.order.service;

import com.example.opsdemo.order.client.FakeProductClient;
import com.example.opsdemo.order.model.OrderPreviewResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        orderService = new OrderService(new FakeProductClient(999L), meterRegistry);
    }

    @Test
    void shouldCalculateOrderPreview() {
        OrderPreviewResponse response = orderService.previewOrder(1L, 2);

        assertEquals(1L, response.productId());
        assertEquals("机械键盘", response.productName());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("798.00"), response.totalAmount());
        assertTrue(response.previewNo().startsWith("PREVIEW-"));
        assertEquals(
                1.0,
                meterRegistry.get("ops.demo.order.preview.requests")
                        .tag("result", "success")
                        .counter()
                        .count()
        );
    }

    @Test
    void shouldReportMissingProduct() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.previewOrder(999L, 1)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Product not found: 999", exception.getReason());

        assertEquals(
                1.0,
                meterRegistry.get("ops.demo.order.preview.requests")
                        .tag("result", "failure")
                        .counter()
                        .count()
        );
    }
}

