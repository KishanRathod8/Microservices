package com.Microservices.InventoryService.dto;


public record InventoryResponse(
        String sku,
        Integer quantity,
        boolean isInStock
) {
    public static InventoryResponse of(String sku, Integer quantity) {
        return new InventoryResponse(sku, quantity, quantity != null && quantity > 0);
    }
}