package com.example.productservice.service.impl;

import com.example.productservice.dto.*;
import com.example.productservice.dto.request.CreateProductReq;
import com.example.productservice.dto.request.ProductFilter;
import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.ApplicationException;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.CategoryRepository;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    public Product create(CreateProductReq createProductReq){
        Optional<Category> existCategory = categoryRepository.findById(createProductReq.getCategoryId());
        if(existCategory.isEmpty()){
            throw new ApplicationException("category not found");
        }
        Product createProduct = productMapper.mapProduct(createProductReq);
        productRepository.save(createProduct);
        return productRepository.save(createProduct);
    }

    @Override
    public List<ProductDto> search(ProductFilter productFilter) {
        List<Product> productList = productRepository.findAllById(productFilter.getIds());
        return productList.stream()
                .map(productMapper::mapDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderConfirm updateQuantity(List<OrderItemDto> orderItemDtos , OrderDto orderDto) {

        List<String> productIds = orderItemDtos.stream().map(OrderItemDto::getProductId).toList();

        List<Product> products = productRepository.findByIdForUpdate(productIds);

        Map<String, Product> productMap = products.stream().collect(
                Collectors.toMap(
                        Product::getId,product->product
                )
        );

        for (OrderItemDto orderItemDto : orderItemDtos) {
            Product product = productMap.get(orderItemDto.getProductId());
            if (product == null) {
                throw new ApplicationException("product not found");
            }
            if(product.getStock() < orderItemDto.getQuantity()){
                throw new ApplicationException("product not enough");
            }
            product.setStock(product.getStock() - orderItemDto.getQuantity());
        }
        return confirmOrder(orderDto);
    }
    public OrderConfirm confirmOrder(OrderDto order) {
        try {
            OrderConfirm event = OrderConfirm.builder()
                    .orderId(order.getId())
                    .status(OrderStatus.CONFIRMED.name())
                    .build();
            kafkaTemplate.send("order-confirmed", event);

            return event;
        } catch (Exception e) {
            OrderConfirm event = OrderConfirm.builder()
                    .orderId(order.getId())
                    .status(OrderStatus.CANCELLED.name())
                    .build();

            kafkaTemplate.send("order-confirmed", event);
            return event;
        }
    }
}
