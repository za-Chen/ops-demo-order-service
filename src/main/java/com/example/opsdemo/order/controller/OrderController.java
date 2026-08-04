package com.example.opsdemo.order.controller;

import com.example.opsdemo.order.model.OrderPreviewResponse;
import com.example.opsdemo.order.service.OrderService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/preview")
    public OrderPreviewResponse previewOrder(
            @RequestParam @Positive long productId,
            @RequestParam(defaultValue = "1") @Min(1) @Max(100) int quantity
    ) {
        return orderService.previewOrder(productId, quantity);
    }
}

