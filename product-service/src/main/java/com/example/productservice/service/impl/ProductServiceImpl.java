package com.example.productservice.service.impl;

import com.example.productservice.dto.*;
import com.example.productservice.document.ProductDocument;
import com.example.productservice.dto.request.CreateProductReq;
import com.example.productservice.dto.request.ProductFilter;
import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.StockReservation;
import com.example.productservice.exception.ApplicationException;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.CategoryRepository;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.repository.ProductSearchRepository;
import com.example.productservice.repository.StockReservationRepository;
import com.example.productservice.service.ProductService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductSearchRepository productSearchRepository;
    private final OutboxPublisher outboxPublisher;
    private final ProductSagaResultPublisher sagaResultPublisher;
    private final StockReservationRepository stockReservationRepository;


    @Override
    public Product create(CreateProductReq createProductReq){
        Optional<Category> existCategory = categoryRepository.findById(createProductReq.getCategoryId());
        if(existCategory.isEmpty()){
            throw new ApplicationException("category not found");
        }
        Product createProduct = productMapper.mapProduct(createProductReq);
        Product savedProduct = productRepository.save(createProduct);
        productSearchRepository.save(toDocument(savedProduct));
        return savedProduct;
    }

    @Override
    public List<ProductDto> search(ProductFilter productFilter) {
        List<ProductDocument> documents = new ArrayList<>();
        productSearchRepository.findAllById(productFilter.getIds()).forEach(documents::add);
        return documents.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderConfirm updateQuantity(List<OrderItemDto> orderItemDtos , OrderDto orderDto) {
        OrderDto result = reserve(orderItemDtos, orderDto);
        String topic = "RESERVED".equals(result.getStatus()) ? "inventory-reserved" : "inventory-rejected";
        outboxPublisher.publish(topic, orderDto.getId(), result);
        return OrderConfirm.builder().orderId(orderDto.getId()).status(result.getStatus()).build();
    }

    @Override
    @Transactional
    public void reserveStock(ReserveStockCommand command) {
        if (command.sagaId() == null || command.orderId() == null || command.items() == null
                || command.items().isEmpty() || command.messageId() == null) {
            throw new IllegalArgumentException("ReserveStock command is missing required fields");
        }
        if (stockReservationRepository.existsById(command.messageId())) return;
        if (command.items().stream().anyMatch(item -> item == null || item.getProductId() == null
                || item.getProductId().isBlank() || item.getQuantity() == null || item.getQuantity() <= 0)) {
            throw new IllegalArgumentException("ReserveStock command contains invalid items");
        }

        Map<String, Integer> quantities = command.items().stream()
                .collect(Collectors.groupingBy(OrderItemDto::getProductId,
                        Collectors.summingInt(OrderItemDto::getQuantity)));

        List<String> productIds = quantities.keySet().stream().sorted().toList();
        List<Product> products = productRepository.findByIdForUpdate(productIds);
        Map<String, Product> productMap = products.stream().collect(
                Collectors.toMap(Product::getId, product -> product));
        boolean available = quantities.entrySet().stream().allMatch(entry -> {
            Product product = productMap.get(entry.getKey());
            return product != null && product.getStock() >= entry.getValue();
        });

        StockReservation reservation = new StockReservation();
        reservation.setMessageId(command.messageId());
        reservation.setSagaId(command.sagaId());
        reservation.setOrderId(command.orderId());
        reservation.setStatus(available ? "RESERVED" : "FAILED");
        String failureReason = available ? null : "INSUFFICIENT_STOCK";
        reservation.setFailureReason(failureReason);
        command.items().forEach(item -> reservation.addItem(item.getProductId(), item.getQuantity()));

        if (available) {
            quantities.forEach((productId, quantity) ->
                    productMap.get(productId).setStock(productMap.get(productId).getStock() - quantity));
        }
        stockReservationRepository.save(reservation);
        String eventType = available ? "StockReserved" : "StockReservationFailed";
        sagaResultPublisher.publishReservationResult(new StockReservationResult(
                eventType, command.sagaId(), command.orderId(), command.messageId(),
                reservation.getStatus(), failureReason));
    }

    private OrderDto reserve(List<OrderItemDto> orderItemDtos, OrderDto orderDto) {
        Map<String, Integer> quantities = orderItemDtos.stream()
                .collect(Collectors.groupingBy(
                        OrderItemDto::getProductId,
                        Collectors.summingInt(OrderItemDto::getQuantity)));
        List<String> productIds = quantities.keySet().stream().sorted().toList();
        List<Product> products = productRepository.findByIdForUpdate(productIds);
        Map<String, Product> productMap = products.stream().collect(
                Collectors.toMap(Product::getId, product -> product));

        boolean canReserve = quantities.entrySet().stream().allMatch(entry -> {
            Product product = productMap.get(entry.getKey());
            return product != null
                    && entry.getValue() > 0
                    && product.getStock() >= entry.getValue();
        });

        OrderDto result = orderDto;
        if (canReserve) {
            quantities.forEach((productId, quantity) -> {
                Product product = productMap.get(productId);
                product.setStock(product.getStock() - quantity);
            });
            result.setStatus("RESERVED");
        } else {
            result.setStatus("REJECTED");
        }
        return result;
    }

    @Override
    @Transactional
    public void releaseQuantity(List<OrderItemDto> items) {
        Map<String, Integer> quantities = items.stream().collect(Collectors.groupingBy(
                OrderItemDto::getProductId, Collectors.summingInt(OrderItemDto::getQuantity)));
        List<String> productIds = quantities.keySet().stream().sorted().toList();
        List<Product> products = productRepository.findByIdForUpdate(productIds);
        Map<String, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        quantities.forEach((productId, quantity) -> {
            Product product = productMap.get(productId);
            if (product != null) product.setStock(product.getStock() + quantity);
        });
    }

    private ProductDocument toDocument(Product product) {
        return new ProductDocument(product.getId(), product.getName(), product.getPrice(),
                product.getStock(), product.getCategoryId());
    }

    private ProductDto toDto(ProductDocument document) {
        return new ProductDto(document.getId(), document.getName(), document.getPrice(),
                document.getStock(), document.getCategoryId());
    }
}
