package com.example.paymentservice.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEvent(String id, double totalAmount, List<OrderItemEvent> orderItems) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderItemEvent(String productId, int quantity) { }
}
