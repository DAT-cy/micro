package com.example.orderservice.dto;


import com.example.orderservice.entity.OutboxEvent;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboxDto {
    private String topic;
    private String eventKey;
    private String payload;
}
