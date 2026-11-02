package com.example.orderservice.dto;

import com.example.orderservice.dto.response.OrderItemResponse;

import java.util.List;
import java.util.UUID;

public record ReserveStockCommand(
        String command,
        UUID sagaId,
        String orderId,
        List<OrderItemResponse> items,
        UUID messageId) {
}
