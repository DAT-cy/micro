package com.example.orderservice.mapper;

import com.example.orderservice.dto.CreateOrderItemDto;
import com.example.orderservice.dto.response.OrderItemResponse;
import com.example.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponse mapDto(OrderItem orderItem);

    OrderItem mapEntity(CreateOrderItemDto CreateOrderItemDto);

    List<OrderItemResponse> mapDtoList(List<OrderItem> items);
}