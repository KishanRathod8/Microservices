package com.Microservices.InventoryService.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        String orderNumber,
        String skuCode,
        BigDecimal price,
        Integer quantity
) {}