package com.example.orderservice.entity;

import com.example.orderservice.dto.OutboxStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    private String id;

    private String topic;
    private String eventKey;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private int retryCount;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    private Instant createdAt;
    private Instant sentAt;

    public OutboxEvent(String topic, String eventKey, String payload) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
        this.eventKey = eventKey;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
    }
}
