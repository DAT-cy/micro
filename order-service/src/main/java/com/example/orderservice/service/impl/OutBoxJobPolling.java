package com.example.orderservice.service.impl;

import com.example.orderservice.dto.OrderStatus;
import com.example.orderservice.dto.OutboxStatus;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OutboxEvent;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OutBoxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutBoxJobPolling {

    private static final int BATCH_SIZE = 10;
    private static final int MAX_RETRIES = 3;

    private final OutBoxEventRepository repository;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.polling-delay-ms:2000}")
    @Transactional
    public void jobPollingOutBox() {
        List<OutboxEvent> events = repository.lockPendingBatch(BATCH_SIZE);
        for (OutboxEvent event : events) {
            try {
                Object payload = objectMapper.readTree(event.getPayload());
                kafkaTemplate.send(event.getTopic(), event.getEventKey(), payload)
                        .get(10, TimeUnit.SECONDS);

                event.setStatus(OutboxStatus.SENT);
                event.setSentAt(Instant.now());
                event.setLastError(null);
            } catch (Exception exception) {
                int retryCount = event.getRetryCount() + 1;
                event.setRetryCount(retryCount);
                event.setLastError(exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage());

                if (retryCount > MAX_RETRIES) {
                    event.setStatus(OutboxStatus.FAILED);
                    if ("order-created".equals(event.getTopic())) {
                        orderRepository.findById(event.getEventKey()).ifPresent(order -> {
                            order.setStatus(OrderStatus.CANCELLED.name());
                            orderRepository.save(order);
                        });
                    }
                    log.error("Outbox event reached retry limit and was marked FAILED, id={}, retryCount={}",
                            event.getId(), retryCount, exception);
                } else {
                    log.warn("Failed to publish outbox event id={}, retryCount={}",
                            event.getId(), retryCount, exception);
                }
            }
        }

        repository.saveAll(events);
    }
}
