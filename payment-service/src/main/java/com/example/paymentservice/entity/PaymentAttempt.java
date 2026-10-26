package com.example.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.example.paymentservice.enums.PaymentAttemptStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_attempts")
@Getter
@NoArgsConstructor
public class PaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentAttemptStatus status;

    @Column(length = 100)
    private String providerReference;

    @Column(length = 100)
    private String failureCode;

    @Column(length = 500)
    private String failureMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public PaymentAttempt(Payment payment, int attemptNumber, PaymentAttemptStatus status,
                          String providerReference, String failureCode, String failureMessage) {
        this.payment = payment;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.providerReference = providerReference;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.createdAt = Instant.now();
    }
}
