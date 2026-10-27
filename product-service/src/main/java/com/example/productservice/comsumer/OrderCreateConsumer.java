package com.example.productservice.comsumer;

import com.example.productservice.dto.OrderDto;
import com.example.productservice.dto.OrderItemDto;
import com.example.productservice.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
public class OrderCreateConsumer {

    private final JsonMapper jsonMapper;

    private final ProductService productService;
    private final RedissonClient redissonClient;

    @KafkaListener(topics = "order-created")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            log.info("Nhận order-created: key={}, partition={}, payload={}",
                    record.key(), record.partition(), record.value());
            OrderDto orderDto = jsonMapper.readValue(record.value(), OrderDto.class);
            List<String> productIdS = orderDto.getOrderItems().stream().map(OrderItemDto::getProductId).distinct().sorted().toList();
            List<RLock> locks = productIdS.stream().map(id -> redissonClient.getLock("product:stock:"+id)).toList();

            RLock multilock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));
            boolean locked = multilock.tryLock(4, TimeUnit.SECONDS);

            try {
                if (!locked) {
                    throw new IllegalStateException(
                            "Cannot acquire locks for all products");
                }
            productService.updateQuantity(orderDto.getOrderItems(),orderDto);
            }finally {
                if(locked) {
                    multilock.unlock();
                }
            }
            log.info("Received order: {}", orderDto);
        } catch (Exception e) {
            log.error("Error processing order: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}