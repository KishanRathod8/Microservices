package com.Microservices.ProductService.dto;

public record InventoryResponse(
        String sku,
        Integer quantity,
        boolean isInStock
) {}