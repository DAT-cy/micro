package com.example.orderservice.repository;

import com.example.orderservice.dto.response.OrderItemResponse;
import com.example.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    @Query("""
        SELECT new com.example.orderservice.dto.response.OrderItemResponse(
            i.id, i.orderId, i.productId, i.price, i.quantity
        )
        FROM OrderItem i
        WHERE i.orderId IN :orderIds
        """)
    List<OrderItemResponse> findByOrderIdIn(@Param("orderIds") List<String> orderIds);

    @Query("""
        SELECT new com.example.orderservice.dto.response.OrderItemResponse(
            i.id, i.orderId, i.productId, i.price, i.quantity
        )
        FROM OrderItem i
        WHERE i.orderId = :orderId
        """)
    List<OrderItemResponse> findByOrderId(@Param("orderId") String orderId);

}
