package com.example.productservice.dto;

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
