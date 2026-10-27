package com.example.productservice.service.impl;

import com.example.productservice.entity.OutboxEvent;
import com.example.productservice.repository.OutBoxEventRepository;
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
    private static final int BATCH_SIZE = 20;
    private static final int MAX_ATTEMPTS = 5;

    private final OutBoxEventRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.polling-delay-ms:2000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = repository.lockPendingBatch(BATCH_SIZE);
        for (OutboxEvent event : events) {
            try {
                Object payload = objectMapper.readTree(event.getPayload());
                kafkaTemplate.send(event.getTopic(), event.getEventKey(), payload)
                        .get(10, TimeUnit.SECONDS);
                event.markPublished();
            } catch (Exception e) {
                if (event.getPublishAttempts() + 1 >= MAX_ATTEMPTS) {
                    event.markFailed(e);
                    log.error("Outbox event permanently failed, id={}", event.getId(), e);
                } else {
                    event.markPublishFailed(e);
                    log.warn("Outbox event publish failed, id={}, attempts={}",
                            event.getId(), event.getPublishAttempts(), e);
                }
            }
        }
    }
}
