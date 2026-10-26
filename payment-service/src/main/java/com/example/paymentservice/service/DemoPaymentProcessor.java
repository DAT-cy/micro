package com.example.paymentservice.service;

import com.example.paymentservice.enums.PaymentStatus;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class DemoPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentStatus charge(BigDecimal amount, String currency) {
        return PaymentStatus.SUCCEEDED;
    }
}
