package com.example.paymentservice.consumer;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.PaymentSagaReply;
import com.example.paymentservice.dto.ProcessPaymentCommand;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.service.impl.PaymentSagaResultPublisher;
import com.example.paymentservice.service.impl.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSagaCommandConsumer {
    private final ObjectMapper objectMapper;
    private final PaymentService payments;
    private final PaymentSagaResultPublisher resultPublisher;

    @KafkaListener(topics = "payment.commands")
    @Transactional
    public void handle(ConsumerRecord<String, String> record) throws Exception {
        ProcessPaymentCommand command = objectMapper.readValue(record.value(), ProcessPaymentCommand.class);
        validate(command);

        String idempotencyKey = "saga-payment:" + command.sagaId();
        Payment payment = payments.createForSaga(idempotencyKey,
                new CreatePaymentRequest(command.orderId(), command.amount(), command.currency(),
                        payments.getSelectedGatewayName()));

        boolean succeeded = payment.getStatus() == PaymentStatus.SUCCEEDED;
        resultPublisher.publish(new PaymentSagaReply(
                succeeded ? "PaymentSucceeded" : "PaymentFailed",
                command.sagaId(), command.orderId(), command.messageId(),
                payment.getStatus().name(), succeeded ? null : payment.getGatewayMessage(), payment.getId()));
    }

    private void validate(ProcessPaymentCommand command) {
        if (!"ProcessPayment".equals(command.command()) || command.sagaId() == null
                || command.orderId() == null || command.orderId().isBlank()
                || command.amount() == null || command.amount().signum() <= 0
                || command.currency() == null || command.currency().isBlank()
                || command.messageId() == null) {
            throw new IllegalArgumentException("Invalid ProcessPayment command");
        }
    }
}
