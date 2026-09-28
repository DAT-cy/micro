package com.example.orderservice.consumer;

import com.example.orderservice.dto.OrderConfirm;
import com.example.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class OrderConfirmConsumer {

    private final JsonMapper jsonMapper;
    private final OrderService orderService;

    @KafkaListener(topics = "order-confirmed")
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(
                    delay = 2000,
                    multiplier = 2.0
            )
    )
    public void listen(OrderConfirm orderDto) throws Exception {
        try {
            log.info("========== BẮT ĐẦU XỬ LÝ MESSAGE ==========");
            log.info("Nội dung order: {}", orderDto);
            orderService.orderConfirm(orderDto);
            log.info("========== XỬ LÝ THÀNH CÔNG ==========");
        } catch (Exception e) {
            log.error("========== LỖI RỒI BẠN ƠI ==========");
            log.error("Nguyên nhân lỗi: {}", e.getMessage(), e);
            log.error("======================================");
            throw e; // Ném ra lại để Spring Kafka xử lý retry
        }
    }

    @DltHandler
    public void processDltMessage(String message, @org.springframework.messaging.handler.annotation.Header(org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("MESSAGE VÀO DLT - Không thể xử lý! Từ topic: {}, Nội dung: {}", topic, message);
    }
}
