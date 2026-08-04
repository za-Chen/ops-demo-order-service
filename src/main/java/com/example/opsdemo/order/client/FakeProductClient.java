package com.example.opsdemo.order.client;

import com.example.opsdemo.order.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class FakeProductClient {

    private static final Logger log = LoggerFactory.getLogger(FakeProductClient.class);

    private static final Map<Long, Product> PRODUCTS = Map.of(
            1L, new Product(1L, "机械键盘", new BigDecimal("399.00")),
            2L, new Product(2L, "无线鼠标", new BigDecimal("129.00")),
            3L, new Product(3L, "27英寸显示器", new BigDecimal("1699.00"))
    );

    private final long faultProductId;

    public FakeProductClient(@Value("${demo.product-client.fault-product-id:999}") long faultProductId) {
        this.faultProductId = faultProductId;
    }

    /**
     * 模拟商品服务调用。productId=999 时故意模拟上游返回空对象。
     */
    public Product findById(long productId) {
        log.info("Calling fake product service, productId={}", productId);

        if (productId == faultProductId) {
            log.warn("Fake product service returned an empty body, productId={}", productId);
            return null;
        }

        Product product = PRODUCTS.get(productId);
        if (product == null) {
            log.info("Product not found in static catalog; returning generated demo product, productId={}", productId);
            return new Product(
                    productId,
                    "演示商品-" + productId,
                    BigDecimal.valueOf(10 + productId % 90).setScale(2)
            );
        }
        return product;
    }
}

