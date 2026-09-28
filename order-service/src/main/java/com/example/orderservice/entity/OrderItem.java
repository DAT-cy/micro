package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_items")
@Entity
@Builder
public class OrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String orderId;
    private String productId;
    private Double price;
    private int quantity;
}
