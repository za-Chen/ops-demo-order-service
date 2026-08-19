package com.example.opsdemo.order.service;

import com.example.opsdemo.order.client.FakeProductClient;
import com.example.opsdemo.order.model.OrderPreviewResponse;
import com.example.opsdemo.order.model.Product;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final FakeProductClient productClient;
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Timer previewTimer;

    public OrderService(FakeProductClient productClient, MeterRegistry meterRegistry) {
        this.productClient = productClient;
        this.successCounter = Counter.builder("ops.demo.order.preview.requests")
                .description("Number of order preview requests")
                .tag("result", "success")
                .register(meterRegistry);
        this.failureCounter = Counter.builder("ops.demo.order.preview.requests")
                .description("Number of order preview requests")
                .tag("result", "failure")
                .register(meterRegistry);
        this.previewTimer = Timer.builder("ops.demo.order.preview.duration")
                .description("Order preview processing duration")
                .publishPercentileHistogram()
                .register(meterRegistry);
    }

    public OrderPreviewResponse previewOrder(long productId, int quantity) {
        long startedAt = System.nanoTime();
        log.info("Starting order preview, productId={}, quantity={}", productId, quantity);

        try {
            Product product = productClient.findById(productId);
            if (product == null) {
                throw new IllegalArgumentException("Product not found, productId=" + productId);
            }

            String productName = product.getName();

            BigDecimal unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = unitPrice
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderPreviewResponse response = new OrderPreviewResponse(
                    "PREVIEW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    product.getId(),
                    productName,
                    quantity,
                    unitPrice,
                    totalAmount,
                    Instant.now()
            );

            successCounter.increment();
            log.info(
                    "Order preview completed, previewNo={}, productId={}, totalAmount={}",
                    response.previewNo(), productId, totalAmount
            );
            return response;
        } catch (RuntimeException exception) {
            failureCounter.increment();
            log.error(
                    "Order preview failed, productId={}, quantity={}, exceptionType={}, message={}",
                    productId,
                    quantity,
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception
            );
            throw exception;
        } finally {
            previewTimer.record(Duration.ofNanos(System.nanoTime() - startedAt));
        }
    }
}

