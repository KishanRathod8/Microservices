package com.Microservices.order_service.listener;

import com.Microservices.order_service.event.OrderFailedEvent;
import com.Microservices.order_service.model.Order;
import com.Microservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderFailedEventListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order-failed-events", groupId = "order-failure-group")
    @Transactional
    public void handleOrderFailedEvent(OrderFailedEvent event) {
        log.warn("Order Service: Received OrderFailedEvent for order: {}. Reason: {}",
                event.orderNumber(), event.reason());

        // Find the order and mark it as cancelled/failed
        Order order = orderRepository.findByOrderNumber(event.orderNumber())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderNumber()));

        order.setStatus("CANCELLED"); // or "FAILED"
        orderRepository.save(order);

        log.info("Order {} status updated to CANCELLED due to inventory failure.", event.orderNumber());
    }
}