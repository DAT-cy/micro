package com.example.productservice.comsumer;

import com.example.productservice.dto.OrderDto;
import com.example.productservice.dto.OrderItemDto;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateConsumer {
    private final JsonMapper jsonMapper;
    private final ProductService productService;
    private final RedissonClient redissonClient;

    @KafkaListener(topics = "order-created")
    public void listenChoreography(ConsumerRecord<String, String> record) {
        process(record.value(), false);
    }

    @KafkaListener(topics = "inventory-release")
    public void release(ConsumerRecord<String, String> record) {
        process(record.value(), true);
    }

    private void process(String payload, boolean release) {
        try {
            OrderDto order = jsonMapper.readValue(payload, OrderDto.class);
            List<String> productIds = order.getOrderItems().stream()
                    .map(OrderItemDto::getProductId).distinct().sorted().toList();
            List<RLock> locks = productIds.stream()
                    .map(id -> redissonClient.getLock("product:stock:" + id)).toList();
            RLock lock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));
            boolean locked = lock.tryLock(4, TimeUnit.SECONDS);
            if (!locked) throw new IllegalStateException("Cannot acquire locks for all products");
            try {
                if (release) productService.releaseQuantity(order.getOrderItems());
                else productService.updateQuantity(order.getOrderItems(), order);
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("Error processing order event", e);
            throw new RuntimeException(e);
        }
    }
}
