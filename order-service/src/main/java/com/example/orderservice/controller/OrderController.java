package com.example.orderservice.controller;

import com.example.orderservice.dto.BaseResponse;
import com.example.orderservice.dto.CreateOrderDto;
import com.example.orderservice.dto.UpdateOrderDto;
import com.example.orderservice.dto.response.OrderDto;
import com.example.orderservice.service.OrderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/order")
public class OrderController {



    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderDto>> createOrder(@RequestBody CreateOrderDto createOrderDto){
        return ResponseEntity.ok(
                new BaseResponse<>(orderService.createOrder(createOrderDto),"created")
        );
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Page<OrderDto>>> getListOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return ResponseEntity.ok(
                new BaseResponse<>(orderService.getAllOrder(page, pageSize), "list")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderDto>> getDetailOrders(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(
                new BaseResponse<>(orderService.getOrderById(id), "detail")
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderDto>> updateOrder(
            @PathVariable String id,
            @RequestBody UpdateOrderDto updateOrderDto
    ) {
        return ResponseEntity.ok(
                new BaseResponse<>(orderService.updateOrder(id, updateOrderDto), "updated")
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Boolean>> deleteOrder(@PathVariable String id) {
        return ResponseEntity.ok(
                new BaseResponse<>(orderService.deleteOrder(id), "deleted")
        );
    }
}
