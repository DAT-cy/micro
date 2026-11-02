package com.example.paymentservice.consumer;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.OrderEvent;
import com.example.paymentservice.service.impl.PaymentService;
import com.example.paymentservice.service.impl.OutboxPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReservedConsumer {
    private final ObjectMapper mapper;
    private final PaymentService payments;
    private final OutboxPublisher outbox;

    @KafkaListener(topics = "inventory-reserved")
    @Transactional
    public void handle(ConsumerRecord<String, String> record) throws Exception {
        OrderEvent order = mapper.readValue(record.value(), OrderEvent.class);
        String idempotencyKey = "order:" + order.id();
        payments.create(idempotencyKey,
                new CreatePaymentRequest(order.id(), BigDecimal.valueOf(order.totalAmount()), "VND",
                        payments.getSelectedGatewayName()));
        outbox.publish("payment-succeeded", order.id(), order);
    }
}
