package com.example.orderservice.controller;

import com.example.orderservice.dto.BaseResponse;
import com.example.orderservice.dto.CreateOrderDto;
import com.example.orderservice.dto.response.OrderDto;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/order")
public class OrderV2Controller {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderDto>> createOrder(
            @RequestBody CreateOrderDto createOrderDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.accepted().body(
                new BaseResponse<>(orderService.createOrchestratedOrder(createOrderDto, idempotencyKey), "created")
        );
    }
}
