package com.example.orderservice.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderConfirm {
    private String orderId;
    private String status;
}
