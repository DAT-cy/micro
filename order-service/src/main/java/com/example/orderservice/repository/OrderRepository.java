package com.example.orderservice.repository;

import com.example.orderservice.dto.response.OrderDto;
import com.example.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("""
        SELECT new com.example.orderservice.dto.response.OrderDto(
            o.id, o.customerId, o.status, o.totalAmount
        )
        FROM Order o
        """)
    Page<OrderDto> findAllOrderDto(Pageable pageable);
}
