package com.example.productservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private String id;
    private String productId;
    private String orderId;
    private Double price;
    private Integer quantity;
}
