package com.Microservices.InventoryService.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DeadLetterPublishingRecoverer publisher(KafkaOperations<String, Object> template) {
        // Publishes failed messages to the original topic name with ".DLQ" appended (e.g., order-events.DLQ)
        return new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + ".DLQ", record.partition()));
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        // Retry 3 times with a 2-second delay between attempts before sending to DLQ
        FixedBackOff backOff = new FixedBackOff(2000L, 3L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Optional: Exceptions that should NOT retry and go straight to DLQ
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);

        return errorHandler;
    }

    @Bean
    public NewTopic orderEventsDlq() {
        return TopicBuilder.name("order-events.DLQ")
                .partitions(1)
                .replicas(1)
                .build();
    }
}