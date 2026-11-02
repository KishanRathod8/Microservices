package com.Microservices.order_service.service;

import com.Microservices.order_service.dto.OrderRequest;
import com.Microservices.order_service.event.OrderCreatedEvent;
import com.Microservices.order_service.model.Order;
import com.Microservices.order_service.model.OutboxEvent;
import com.Microservices.order_service.repository.OrderRepository;
import com.Microservices.order_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void placeOrder(OrderRequest orderRequest) {
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .skuCode(orderRequest.skuCode())
                .price(orderRequest.price())
                .quantity(orderRequest.quantity())
                .status("PENDING")
                .build();

        orderRepository.save(order);
        log.info("Order saved successfully with order number: {}", order.getOrderNumber());

        // Create the event payload
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getOrderNumber(),
                order.getSkuCode(),
                order.getQuantity(),
                "customer@example.com"
        );

        try {
            // Serialize event into JSON string
            String payloadJson = objectMapper.writeValueAsString(event);

            // Save to Outbox table atomically within the same transaction
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("ORDER")
                    .aggregateId(order.getOrderNumber())
                    .eventType("OrderCreatedEvent")
                    .payload(payloadJson)
                    .createdAt(Instant.now())
                    .processed(false)
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Outbox event saved atomically for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to serialize order event for outbox", e);
            throw new RuntimeException("Failed to serialize order event for outbox", e);
        }
    }
}