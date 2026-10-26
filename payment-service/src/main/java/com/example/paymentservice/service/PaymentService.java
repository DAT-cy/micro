package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final PaymentProcessor processor;

    public PaymentService(PaymentRepository payments, PaymentProcessor processor) {
        this.payments = payments;
        this.processor = processor;
    }

    @Transactional
    public Payment create(String idempotencyKey, CreatePaymentRequest request) {
        var existing = payments.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            if (!payment.getOrderId().equals(request.orderId())
                    || payment.getAmount().compareTo(request.amount()) != 0
                    || !payment.getCurrency().equalsIgnoreCase(request.currency())) {
                throw new IllegalArgumentException("Idempotency-Key was already used with a different payment");
            }
            return payment;
        }

        Payment payment = new Payment(idempotencyKey, request.orderId(), request.amount(),
                request.currency().toUpperCase(), processor.charge(request.amount(), request.currency()));
        try {
            return payments.saveAndFlush(payment);
        } catch (DataIntegrityViolationException race) {
            return payments.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> race);
        }
    }

    @Transactional(readOnly = true)
    public Payment get(UUID id) {
        return payments.findById(id).orElseThrow(() -> new EntityNotFoundException("Payment not found"));
    }
}
