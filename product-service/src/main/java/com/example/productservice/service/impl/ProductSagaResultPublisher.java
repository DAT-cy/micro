package com.example.productservice.service.impl;

import com.example.productservice.dto.StockReservationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductSagaResultPublisher {
    private static final String ORDER_SAGA_REPLIES_TOPIC = "order.saga.replies";

    private final OutboxPublisher outboxPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishReservationResult(StockReservationResult result) {
        outboxPublisher.publish(ORDER_SAGA_REPLIES_TOPIC, result.orderId(), result);
    }
}
