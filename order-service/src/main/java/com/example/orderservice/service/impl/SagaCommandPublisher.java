package com.example.orderservice.service.impl;

import com.example.orderservice.dto.ReserveStockCommand;
import com.example.orderservice.dto.ProcessPaymentCommand;
import com.example.orderservice.dto.response.OrderDto;
import com.example.orderservice.dto.response.OrderItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SagaCommandPublisher {
    private static final String PRODUCT_COMMANDS_TOPIC = "product.commands";
    private static final String PAYMENT_COMMANDS_TOPIC = "payment.commands";

    private final OutboxPublisher outboxPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishReserveStock(UUID sagaId, OrderDto order, List<OrderItemResponse> items, UUID messageId) {
        outboxPublisher.publish(PRODUCT_COMMANDS_TOPIC, order.getId(),
                new ReserveStockCommand("ReserveStock", sagaId, order.getId(), items, messageId));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishProcessPayment(UUID sagaId, String orderId, BigDecimal amount,
                                      String currency, UUID messageId) {
        outboxPublisher.publish(PAYMENT_COMMANDS_TOPIC, orderId,
                new ProcessPaymentCommand("ProcessPayment", sagaId, orderId, amount, currency, messageId));
    }
}
