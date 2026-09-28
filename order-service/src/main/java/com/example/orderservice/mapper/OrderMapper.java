package com.example.orderservice.mapper;

import com.example.orderservice.dto.CreateOrderDto;
import com.example.orderservice.dto.response.OrderDto;
import com.example.orderservice.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring" , uses = OrderItemMapper.class)
public interface OrderMapper {
    OrderDto mapDto(Order order);
    Order mapEntity(CreateOrderDto createOrderDto);

}
