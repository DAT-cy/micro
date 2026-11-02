package com.example.orderservice.dto;

import java.util.UUID;

public record SagaReply(
        String eventType,
        UUID sagaId,
        String orderId,
        UUID messageId,
        String status,
        String reason,
        UUID paymentId) {
}
