package com.example.productservice.comsumer;

import com.example.productservice.dto.OrderDto;
import com.example.productservice.dto.OrderItemDto;
import com.example.productservice.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class OrderCreateConsumer {

    private final JsonMapper jsonMapper;

    private final ProductService productService;

    @KafkaListener(topics = "order-created")
    public void listen(String orderString) {
        try {
            OrderDto orderDto = jsonMapper.readValue(
                    orderString,
                    OrderDto.class
            );
            List<OrderItemDto> orderItems = orderDto.getOrderItems();
            productService.updateQuantity(orderItems , orderDto);
            log.info("Received order: {}", orderDto);
        } catch (Exception e) {
            log.error("Error processing order: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}