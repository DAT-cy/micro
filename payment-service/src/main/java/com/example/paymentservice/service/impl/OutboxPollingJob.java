package com.example.paymentservice.service.impl;

import com.example.paymentservice.entity.OutboxEvent;
import com.example.paymentservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollingJob {
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, Object> kafka;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.polling-delay-ms:2000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = repository.lockPendingBatch(20);
        for (OutboxEvent event : events) {
            try {
                Object payload = objectMapper.readTree(event.getPayload());
                kafka.send(event.getEventType(), event.getAggregateId(), payload).get(10, TimeUnit.SECONDS);
                event.markPublished();
            } catch (Exception e) {
                boolean permanentlyFailed = event.markPublishFailed();
                if (permanentlyFailed) {
                    log.error("Payment outbox event exhausted retries, id={}", event.getId(), e);
                } else {
                    log.warn("Failed to publish payment outbox event id={}, attempts={}",
                            event.getId(), event.getPublishAttempts(), e);
                }
            }
        }
    }
}
