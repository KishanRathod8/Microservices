package com.Microservices.ProductService.dto;

import com.Microservices.ProductService.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        Integer availableStock,
        Instant createdAt
) {
    public static ProductResponse fromEntity(Product product, Integer availableStock) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                availableStock,
                product.getCreatedAt()
        );
    }
}