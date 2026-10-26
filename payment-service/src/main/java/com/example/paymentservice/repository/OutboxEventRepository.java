package com.example.paymentservice.repository;

import com.example.paymentservice.entity.OutboxEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
}
