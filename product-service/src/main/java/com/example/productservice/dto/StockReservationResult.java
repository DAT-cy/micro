package com.example.productservice.dto;

import java.util.UUID;

public record StockReservationResult(
        String eventType,
        UUID sagaId,
        String orderId,
        UUID messageId,
        String status,
        String reason) {
}
