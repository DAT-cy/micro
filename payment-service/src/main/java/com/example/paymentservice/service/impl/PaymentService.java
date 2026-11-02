package com.example.paymentservice.service.impl;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentProcessor;
import com.example.paymentservice.service.PaymentProcessorResolver;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final PaymentProcessorResolver processorResolver;

    public PaymentService(PaymentRepository payments, PaymentProcessorResolver processorResolver) {
        this.payments = payments;
        this.processorResolver = processorResolver;
    }

    @Transactional
    public Payment create(String idempotencyKey, CreatePaymentRequest request) {
        return createPayment(idempotencyKey, request, false);
    }

    @Transactional
    public Payment createForSaga(String idempotencyKey, CreatePaymentRequest request) {
        return createPayment(idempotencyKey, request, true);
    }

    private Payment createPayment(String idempotencyKey, CreatePaymentRequest request, boolean captureFailure) {
        var existing = payments.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            if (!payment.getOrderId().equals(request.orderId())
                    || payment.getAmount().compareTo(request.amount()) != 0
                    || !payment.getCurrency().equalsIgnoreCase(request.currency())
                    || !request.gateway().equalsIgnoreCase(payment.getGateway())) {
                throw new IllegalArgumentException("Idempotency-Key was already used with a different payment");
            }
            return payment;
        }

        PaymentProcessor processor = processorResolver.resolve(request.gateway());
        Payment payment = new Payment(
                idempotencyKey,
                request.orderId(),
                request.amount(),
                request.currency().toUpperCase(),
                com.example.paymentservice.enums.PaymentStatus.PROCESSING);
        payment.setGateway(processor.gatewayName());
        try {
            payments.saveAndFlush(payment);
        } catch (DataIntegrityViolationException race) {
            return payments.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> race);
        }

        try {
            payment.setGatewayMessage("Payment gateway selected: " + processor.processPayment());
            payment.markSucceeded();
        } catch (RuntimeException gatewayFailure) {
            if (!captureFailure) throw gatewayFailure;
            payment.markFailed();
            payment.setGatewayMessage(gatewayFailure.getMessage() == null
                    ? gatewayFailure.getClass().getSimpleName()
                    : gatewayFailure.getMessage());
        }
        return payments.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment get(UUID id) {
        return payments.findById(id).orElseThrow(() -> new EntityNotFoundException("Payment not found"));
    }

    public String getSelectedGatewayName() {
        return processorResolver.selected().gatewayName();
    }
}
