package com.example.productservice.comsumer;

import com.example.productservice.dto.OrderItemDto;
import com.example.productservice.dto.ReserveStockCommand;
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
public class ProductSagaCommandConsumer {
    private final JsonMapper jsonMapper;
    private final ProductService productService;
    private final RedissonClient redissonClient;

    @KafkaListener(topics = "product.commands")
    public void handle(ConsumerRecord<String, String> record) {
        try {
            ReserveStockCommand command = jsonMapper.readValue(record.value(), ReserveStockCommand.class);
            if (!"ReserveStock".equals(command.command())) {
                throw new IllegalArgumentException("Unsupported product command: " + command.command());
            }
            if (command.items() == null || command.items().isEmpty()) {
                throw new IllegalArgumentException("ReserveStock command has no items");
            }
            List<String> productIds = command.items().stream()
                    .map(OrderItemDto::getProductId).distinct().sorted().toList();
            List<RLock> locks = productIds.stream()
                    .map(id -> redissonClient.getLock("product:stock:" + id)).toList();
            RLock lock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));
            boolean locked = lock.tryLock(4, TimeUnit.SECONDS);
            if (!locked) throw new IllegalStateException("Cannot acquire locks for all products");
            try {
                productService.reserveStock(command);
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("Error processing product saga command", e);
            throw new RuntimeException(e);
        }
    }
}
