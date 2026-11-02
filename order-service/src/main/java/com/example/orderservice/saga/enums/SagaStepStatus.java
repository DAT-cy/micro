package com.example.orderservice.saga.enums;

public enum SagaStepStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}