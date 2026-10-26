package com.example.paymentservice.service;

import com.example.paymentservice.enums.PaymentStatus;
import java.math.BigDecimal;

public interface PaymentProcessor {
    PaymentStatus charge(BigDecimal amount, String currency);
}
