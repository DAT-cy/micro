package com.example.orderservice.consumer;

import com.example.orderservice.dto.OrderStatus;
import com.example.orderservice.dto.response.OrderDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.impl.OutboxPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class OrderSagaConsumer {
    private final ObjectMapper objectMapper;
    private final OrderRepository orders;
    private final OutboxPublisher outbox;

    @KafkaListener(topics = {"inventory-rejected", "payment-succeeded", "payment-failed"})
    @Transactional
    public void handle(ConsumerRecord<String, String> record) throws Exception {
        OrderDto event = objectMapper.readValue(record.value(), OrderDto.class);
        Order order = orders.findById(event.getId()).orElseThrow();
        switch (record.topic()) {
            case "inventory-rejected", "payment-failed" -> order.setStatus(OrderStatus.CANCELLED.name());
            case "payment-succeeded" -> order.setStatus(OrderStatus.CONFIRMED.name());
            default -> { return; }
        }
        orders.save(order);
        if ("payment-failed".equals(record.topic())) {
            outbox.publish("inventory-release", order.getId(), event);
        }
    }
}
