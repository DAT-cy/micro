package com.example.paymentservice.dto;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID paymentId, String orderId, BigDecimal amount, String currency,
                              PaymentStatus status, Instant createdAt) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getAmount(),
                payment.getCurrency(), payment.getStatus(), payment.getCreatedAt());
    }
}
