package com.example.orderservice.consumer;

import com.example.orderservice.dto.OrderStatus;
import com.example.orderservice.dto.SagaReply;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.saga.entity.SagaInstance;
import com.example.orderservice.saga.entity.SagaStep;
import com.example.orderservice.saga.enums.SagaStatus;
import com.example.orderservice.saga.enums.SagaStepStatus;
import com.example.orderservice.saga.repository.SagaInstanceRepository;
import com.example.orderservice.saga.repository.SagaStepRepository;
import com.example.orderservice.service.impl.SagaCommandPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderSagaResultConsumer {
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final SagaInstanceRepository sagaRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaCommandPublisher sagaCommandPublisher;

    @KafkaListener(topics = "order.saga.replies")
    @Transactional
    public void handle(ConsumerRecord<String, String> record) throws Exception {
        SagaReply result = objectMapper.readValue(record.value(), SagaReply.class);
        validate(result);

        UUID orderId = UUID.fromString(result.orderId());
        SagaInstance saga = sagaRepository.findByOrderId(orderId).orElseThrow();
        if (!saga.getId().equals(result.sagaId())) return;

        if ("PaymentSucceeded".equals(result.eventType()) || "PaymentFailed".equals(result.eventType())) {
            handlePaymentResult(result, saga);
            return;
        }

        SagaStep reserveStock = sagaStepRepository.findBySagaId(saga.getId()).stream()
                .filter(step -> "RESERVE_STOCK".equals(step.getStepName()))
                .findFirst()
                .orElseThrow();
        if (!result.messageId().equals(reserveStock.getCommandId())
                || reserveStock.getStatus() != SagaStepStatus.PENDING) return;

        reserveStock.setCompletedAt(LocalDateTime.now());
        if ("StockReserved".equals(result.eventType())) {
            reserveStock.setStatus(SagaStepStatus.COMPLETED);
            Order order = orderRepository.findById(result.orderId()).orElseThrow();
            UUID paymentCommandId = UUID.randomUUID();
            SagaStep paymentStep = new SagaStep();
            paymentStep.setSagaId(saga.getId());
            paymentStep.setStepName("PROCESS_PAYMENT");
            paymentStep.setCommandId(paymentCommandId);
            paymentStep.setStatus(SagaStepStatus.PENDING);
            paymentStep.setStartedAt(LocalDateTime.now());
            sagaStepRepository.save(paymentStep);
            saga.setCurrentStep("PROCESS_PAYMENT");
            sagaCommandPublisher.publishProcessPayment(saga.getId(), order.getId(),
                    BigDecimal.valueOf(order.getTotalAmount()), "VND", paymentCommandId);
        } else {
            reserveStock.setStatus(SagaStepStatus.FAILED);
            saga.setStatus(SagaStatus.FAILED);
            orderRepository.findById(result.orderId()).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED.name());
                orderRepository.save(order);
            });
        }

        sagaStepRepository.save(reserveStock);
        sagaRepository.save(saga);
    }

    private void handlePaymentResult(SagaReply result, SagaInstance saga) {
        SagaStep payment = sagaStepRepository.findBySagaId(saga.getId()).stream()
                .filter(step -> "PROCESS_PAYMENT".equals(step.getStepName()))
                .findFirst()
                .orElseThrow();
        if (!result.messageId().equals(payment.getCommandId())
                || payment.getStatus() != SagaStepStatus.PENDING) return;

        Order order = orderRepository.findById(result.orderId()).orElseThrow();
        payment.setCompletedAt(LocalDateTime.now());
        if ("PaymentSucceeded".equals(result.eventType())) {
            payment.setStatus(SagaStepStatus.COMPLETED);
            saga.setStatus(SagaStatus.COMPLETED);
            saga.setCurrentStep("COMPLETED");
            order.setStatus(OrderStatus.CONFIRMED.name());
        } else {
            payment.setStatus(SagaStepStatus.FAILED);
            saga.setStatus(SagaStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED.name());
        }
        sagaStepRepository.save(payment);
        sagaRepository.save(saga);
        orderRepository.save(order);
    }

    private void validate(SagaReply result) {
        if (result.sagaId() == null || result.orderId() == null || result.messageId() == null
                || !("StockReserved".equals(result.eventType())
                || "StockReservationFailed".equals(result.eventType())
                || "PaymentSucceeded".equals(result.eventType())
                || "PaymentFailed".equals(result.eventType()))) {
            throw new IllegalArgumentException("Invalid stock reservation result");
        }
    }
}
