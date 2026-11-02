package com.example.productservice.dto;

import java.util.List;
import java.util.UUID;

public record ReserveStockCommand(
        String command,
        UUID sagaId,
        String orderId,
        List<OrderItemDto> items,
        UUID messageId) {
}
