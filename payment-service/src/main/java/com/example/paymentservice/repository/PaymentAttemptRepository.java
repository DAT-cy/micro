package com.example.paymentservice.repository;

import com.example.paymentservice.entity.PaymentAttempt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {
    List<PaymentAttempt> findAllByPaymentIdOrderByAttemptNumber(UUID paymentId);
}
