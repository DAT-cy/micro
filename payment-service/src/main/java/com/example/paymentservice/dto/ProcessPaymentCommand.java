package com.example.paymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(
        String command,
        UUID sagaId,
        String orderId,
        BigDecimal amount,
        String currency,
        UUID messageId) {
}
