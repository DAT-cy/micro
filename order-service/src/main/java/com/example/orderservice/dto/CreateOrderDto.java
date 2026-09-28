package com.example.orderservice.dto;

import com.example.orderservice.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderDto {
    private String customerId;
    private List<CreateOrderItemDto> orderItems;
}
