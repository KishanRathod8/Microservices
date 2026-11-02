package com.Microservices.order_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "t_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType; // e.g., "ORDER"
    private String aggregateId;   // e.g., orderNumber
    private String eventType;     // e.g., "OrderCreatedEvent"

    @Column(columnDefinition = "TEXT")
    private String payload;       // JSON string of the event

    private Instant createdAt;
    private boolean processed;
}