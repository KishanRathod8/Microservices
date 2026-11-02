package com.Microservices.notification_service.event;

public record OrderCreatedEvent(
        String orderNumber,
        String skuCode,
        Integer quantity,
        String email
) {}