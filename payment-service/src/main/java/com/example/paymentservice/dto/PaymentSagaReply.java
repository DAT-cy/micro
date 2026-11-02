package com.example.paymentservice.dto;

import java.util.UUID;

public record PaymentSagaReply(
        String eventType,
        UUID sagaId,
        String orderId,
        UUID messageId,
        String status,
        String reason,
        UUID paymentId) {
}
