package com.example.productservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor
public class OutboxEvent {
    public enum Status { PENDING, PUBLISHED, FAILED }

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(name = "event_key", nullable = false, length = 100)
    private String eventKey;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private int publishAttempts;

    @Column(length = 1000)
    private String lastError;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant publishedAt;

    public OutboxEvent(String topic, String eventKey, String payload) {
        this.id = UUID.randomUUID();
        this.topic = topic;
        this.eventKey = eventKey;
        this.payload = payload;
        this.status = Status.PENDING;
        this.createdAt = Instant.now();
    }

    public void markPublished() {
        this.status = Status.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void markPublishFailed(Exception exception) {
        this.publishAttempts++;
        this.lastError = exception.getMessage() == null ? exception.getClass().getSimpleName()
                : exception.getMessage().substring(0, Math.min(1000, exception.getMessage().length()));
    }

    public void markFailed(Exception exception) {
        markPublishFailed(exception);
        this.status = Status.FAILED;
    }
}
