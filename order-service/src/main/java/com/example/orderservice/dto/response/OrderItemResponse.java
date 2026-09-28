package com.example.orderservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private String id;
    private String orderId;
    private String productId;
    private Double price;
    private int quantity;
}
