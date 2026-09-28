package com.example.orderservice.dto.response;

import com.example.orderservice.entity.OrderItem;
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
    private List<OrderItemResponse> orderItems;

    public OrderDto(String id, String customerId, String status, int totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderItems = new ArrayList<>();
    }
}
