package com.example.orderservice.service.impl;

import com.example.orderservice.clients.ProductClient;
import com.example.orderservice.dto.*;
import com.example.orderservice.dto.client.request.ProductFilter;
import com.example.orderservice.dto.response.OrderDto;
import com.example.orderservice.dto.response.OrderItemResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.exception.ApplicationException;
import com.example.orderservice.mapper.OrderItemMapper;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.repository.OrderItemRepository;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductClient productClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderDto createOrderDto) {

        List<String> ids = createOrderDto.getOrderItems().stream().map(CreateOrderItemDto::getProductId).distinct().toList();

        List<ProductDto> productDtos = productClient.getProductByIds(new ProductFilter(ids));

        Map<String , ProductDto> productDtoMap = productDtos.stream().collect(
                Collectors.toMap(
                        ProductDto::getId,
                        product -> product
                )
        );
        Order order = orderMapper.mapEntity(createOrderDto);
        order.setStatus(OrderStatus.PENDING.name());
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> list = new ArrayList<>();
        int totalQuantity = 0;
        double totalAmount = 0;

        for (CreateOrderItemDto createOrderItemDto : createOrderDto.getOrderItems()){
            ProductDto productDto = productDtoMap.get(createOrderItemDto.getProductId());
            if(productDto == null){
                throw new ApplicationException("sản phẩm " + createOrderItemDto.getProductId() + "không tồn tại" );
            }
            if(createOrderItemDto.getQuantity() > productDto.getStock()){
                throw new ApplicationException("Sẩn phẩm " + createOrderItemDto.getQuantity() + "vượt quá trong kho" );
            }
            OrderItem orderItem = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(productDto.getId())
                    .price(productDto.getPrice())
                    .quantity(createOrderItemDto.getQuantity())
                    .build();
            totalQuantity += createOrderItemDto.getQuantity();

            totalAmount += productDto.getPrice()
                    * createOrderItemDto.getQuantity();

            list.add(orderItem);
        }

        List<OrderItem> savedItems = orderItemRepository.saveAll(list);
        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQuantity);
        orderRepository.save(order);
        OrderDto response = orderMapper.mapDto(savedOrder);
        response.setOrderItems(orderItemMapper.mapDtoList(savedItems));

        kafkaTemplate.send("order-created",response);
        log.info("publish new order order-created");
        return response;
    }

    @Override
    public Page<OrderDto> getAllOrder(Integer page , Integer size){
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orderPage = orderRepository.findAllOrderDto(pageable);

        List<String> orderIds = orderPage.getContent().stream().map(OrderDto::getId).toList();

        List<OrderItemResponse> orderItemResponseList = orderItemRepository.findByOrderIdIn(orderIds);

        orderPage.getContent().forEach(orderDto -> {
            List<OrderItemResponse> matchedItems = orderItemResponseList.stream()
                    .filter(item -> item.getOrderId().equals(orderDto.getId()))
                    .toList();
            orderDto.setOrderItems(matchedItems);
        });
        return orderPage;

    }

    @Override
    public OrderDto getOrderById(String id) {

        Order order = validateOrder(id);

        OrderDto orderDto = orderMapper.mapDto(order);
        orderDto.setOrderItems(orderItemRepository.findByOrderId(order.getId()));
        return orderDto;
    }

    @Override
    public OrderDto updateOrder(String id, UpdateOrderDto updateOrderDto) {

        Order order = validateOrder(id);
        order.setStatus(updateOrderDto.getStatus());
        return orderMapper.mapDto(orderRepository.save(order));
    }

    public Order validateOrder(String id){
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()){
            throw new ApplicationException("Order không tổn tại");
        }
        return order.get();
    }

    @Override
    public boolean deleteOrder(String id) {
        Order order = validateOrder(id);
        order.setIsDeleted(true);
        return true;
    }

    @Override
    public OrderConfirm orderConfirm(OrderConfirm orderConfirm){
        Order order = validateOrder(orderConfirm.getOrderId());
        order.setStatus(orderConfirm.getStatus());
        orderRepository.save(order);
        return orderConfirm;
    }
}
