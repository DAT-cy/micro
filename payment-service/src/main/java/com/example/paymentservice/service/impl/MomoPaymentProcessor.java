package com.example.paymentservice.service.impl;

import com.example.paymentservice.service.PaymentProcessor;
import org.springframework.stereotype.Component;

@Component
public class MomoPaymentProcessor implements PaymentProcessor {
    @Override
    public String gatewayName() {
        return "momo";
    }

    @Override
    public String processPayment() {
        return "MoMo";
    }
}
