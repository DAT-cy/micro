package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderDto;
import com.example.orderservice.dto.OrderConfirm;
import com.example.orderservice.dto.UpdateOrderDto;
import com.example.orderservice.dto.response.OrderDto;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.List;

public interface OrderService {
    OrderDto createOrder(CreateOrderDto createOrderDto);
    Page<OrderDto> getAllOrder(Integer page , Integer size);
    OrderDto getOrderById(String id);
    OrderDto updateOrder(String id, UpdateOrderDto updateOrderDto);
    boolean deleteOrder(String id);
    OrderConfirm orderConfirm(OrderConfirm orderConfirm);
}
