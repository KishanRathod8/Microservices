package com.Microservices.InventoryService.event; // or order-service equivalent

public record OrderFailedEvent(
        String orderNumber,
        String skuCode,
        String reason
) {}