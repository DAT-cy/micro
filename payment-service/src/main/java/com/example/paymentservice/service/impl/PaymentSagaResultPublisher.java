package com.example.paymentservice.service.impl;

import com.example.paymentservice.dto.PaymentSagaReply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentSagaResultPublisher {
    private static final String ORDER_SAGA_REPLIES_TOPIC = "order.saga.replies";

    private final OutboxPublisher outboxPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(PaymentSagaReply reply) {
        outboxPublisher.publish(ORDER_SAGA_REPLIES_TOPIC, reply.orderId(), reply);
    }
}
