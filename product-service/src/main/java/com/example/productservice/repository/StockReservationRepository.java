package com.example.productservice.repository;

import com.example.productservice.entity.StockReservation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
}
