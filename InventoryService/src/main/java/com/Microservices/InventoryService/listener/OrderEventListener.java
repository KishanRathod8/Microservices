package com.Microservices.InventoryService.listener;

import com.Microservices.InventoryService.event.OrderCreatedEvent;
import com.Microservices.InventoryService.event.OrderFailedEvent;
import com.Microservices.InventoryService.model.Inventory;
import com.Microservices.InventoryService.model.ProcessedEvent;
import com.Microservices.InventoryService.repository.InventoryRepository;
import com.Microservices.InventoryService.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final InventoryRepository inventoryRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-events", groupId = "inventory-group-v3")
    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Inventory Service: Received OrderCreatedEvent for order: {}", event.orderNumber());

        // 1. Idempotency Check: Prevent duplicate processing if Kafka retries/redelivers
        if (processedEventRepository.existsByOrderNumber(event.orderNumber())) {
            log.warn("Duplicate OrderCreatedEvent detected for order: {}. Skipping processing.", event.orderNumber());

            // Acknowledge the duplicate message so it gets cleared from the offset queue
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            return;
        }

        log.info("Checking stock for SKU: {} with quantity: {}", event.skuCode(), event.quantity());

        Inventory inventory = inventoryRepository.findBySkuCode(event.skuCode())
                .orElseThrow(() -> {
                    log.error("Product SKU not found in inventory: {}", event.skuCode());
                    return new RuntimeException("Product SKU not found in inventory: " + event.skuCode());
                });

        // 2. Check if sufficient stock is available
        if (inventory.getQuantity() >= event.quantity()) {
            // Deduct stock (Saga Step Success)
            inventory.setQuantity(inventory.getQuantity() - event.quantity());
            inventoryRepository.save(inventory);
            log.info("Stock successfully reserved for SKU: {}. Remaining stock: {}", event.skuCode(), inventory.getQuantity());
        } else {
            log.error("Insufficient stock for SKU: {}. Required: {}, Available: {}",
                    event.skuCode(), event.quantity(), inventory.getQuantity());

            // Publish OrderFailedEvent to trigger compensating transaction in Order Service
            OrderFailedEvent failedEvent = new OrderFailedEvent(
                    event.orderNumber(),
                    event.skuCode(),
                    "Insufficient stock in inventory. Required: " + event.quantity() + ", Available: " + inventory.getQuantity()
            );

            kafkaTemplate.send("order-failed-events", failedEvent);
            log.warn("Published OrderFailedEvent for order: {} due to insufficient stock.", event.orderNumber());
        }

        // 3. Record this order as successfully processed in the idempotency ledger
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .orderNumber(event.orderNumber())
                .processedAt(Instant.now())
                .build();

        processedEventRepository.save(processedEvent);
        log.info("Recorded order: {} in processed events ledger.", event.orderNumber());

        // 4. Manually commit offset immediately after successful processing
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
            log.info("Offset manually committed for order: {}", event.orderNumber());
        }
    }
}