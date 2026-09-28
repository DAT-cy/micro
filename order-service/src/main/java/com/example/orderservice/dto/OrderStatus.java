package com.example.orderservice.dto;

public enum OrderStatus {

    PENDING,    // Chờ xử lý
    CONFIRMED,  // Đã xác nhận
    CANCELLED,  // Đã hủy
    COMPLETED   // Hoàn thành
}