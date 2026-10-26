package com.example.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.example.paymentservice.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(nullable = false, length = 100)
    private String orderId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Payment(String idempotencyKey, String orderId, BigDecimal amount, String currency, PaymentStatus status) {
        this.id = UUID.randomUUID();
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = Instant.now();
    }
}
