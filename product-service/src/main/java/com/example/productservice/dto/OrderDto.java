package com.example.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private String id;
    private String customerId;
    private String status;
    private Double totalQuantity;
    private int totalAmount;
    private List<OrderItemDto> orderItems;

    public OrderDto(String id, String customerId, String status, int totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderItems = new ArrayList<>();
    }
}
