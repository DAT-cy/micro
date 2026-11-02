package com.example.paymentservice.service;

public interface PaymentProcessor {
    String gatewayName();

    String processPayment();
}
