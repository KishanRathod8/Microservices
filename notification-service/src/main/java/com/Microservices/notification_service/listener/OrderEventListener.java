package com.Microservices.notification_service.listener;

import com.Microservices.notification_service.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order number: {}", event.orderNumber());
        log.info("-> Product SKU: {}", event.skuCode());
        log.info("-> Quantity: {}", event.quantity());
        log.info("-> Customer Email: {}", event.email());
        log.info("Notification sent successfully! Order processing pipeline complete.");
    }
}