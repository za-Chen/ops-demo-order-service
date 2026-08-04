package com.example.opsdemo.order.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderPreviewResponse(
        String previewNo,
        long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        Instant previewedAt
) {
}

