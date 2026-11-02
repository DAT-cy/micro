package com.example.paymentservice.service.impl;

import com.example.paymentservice.service.PaymentProcessor;
import org.springframework.stereotype.Component;

@Component
public class VnPayPaymentProcessor implements PaymentProcessor {
    @Override
    public String gatewayName() {
        return "vnpay";
    }

    @Override
    public String processPayment() {
        return "VNPay";
    }
}
