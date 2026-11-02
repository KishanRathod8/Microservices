package com.Microservices.order_service.event; // or order-service equivalent

public record OrderFailedEvent(
        String orderNumber,
        String skuCode,
        String reason
) {}