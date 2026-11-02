package com.example.orderservice.saga.entity;

import com.example.orderservice.saga.enums.SagaStepStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "saga_steps"
)
@Getter
@Setter
public class SagaStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "saga_id", nullable = false)
    private UUID sagaId;

    private String stepName;

    @Column(unique = true)
    private UUID commandId;

    @Enumerated(EnumType.STRING)
    private SagaStepStatus status;

    private Integer retryCount = 0;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
