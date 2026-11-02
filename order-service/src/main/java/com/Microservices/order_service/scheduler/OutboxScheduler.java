package com.Microservices.order_service.scheduler;

import com.Microservices.order_service.model.OutboxEvent;
import com.Microservices.order_service.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> unprocessedEvents = outboxRepository.findByProcessedFalse();

        if (!unprocessedEvents.isEmpty()) {
            log.info("Found {} unprocessed outbox events. Publishing to Kafka...", unprocessedEvents.size());
        }

        for (OutboxEvent event : unprocessedEvents) {
            try {
                // Publish JSON payload string to Kafka topic
                kafkaTemplate.send("order-events", event.getAggregateId(), event.getPayload())
                        .get(); // Optional: block briefly to ensure acknowledgment

                // Mark as processed
                event.setProcessed(true);
                outboxRepository.save(event);

                log.info("Successfully published outbox event ID: {} to Kafka", event.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event ID: {} to Kafka. Will retry on next schedule.", event.getId(), e);
                // Break or continue to let other events process
            }
        }
    }
}