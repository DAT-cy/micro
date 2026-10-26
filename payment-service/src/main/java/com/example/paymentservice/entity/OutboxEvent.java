package com.example.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import com.example.paymentservice.enums.OutboxStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor
public class OutboxEvent {
    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String aggregateId;

    @Column(nullable = false, length = 50)
    private String aggregateType;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int publishAttempts;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant publishedAt;

    public OutboxEvent(String aggregateId, String aggregateType, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.publishAttempts = 0;
        this.createdAt = Instant.now();
    }
}
