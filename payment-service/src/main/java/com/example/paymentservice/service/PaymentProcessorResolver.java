package com.example.paymentservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentProcessorResolver {
    private final PaymentProcessor selectedProcessor;
    private final Map<String, PaymentProcessor> processorsByGateway;

    public PaymentProcessorResolver(
            List<PaymentProcessor> processors,
            @Value("${payment.gateway:unconfigured}") String configuredGateway) {
        this.processorsByGateway = processors.stream()
                .collect(Collectors.toMap(PaymentProcessor::gatewayName, Function.identity()));
        this.selectedProcessor = resolve(configuredGateway);
    }

    public PaymentProcessor selected() {
        return selectedProcessor;
    }

    public PaymentProcessor resolve(String gatewayName) {
        PaymentProcessor processor = processorsByGateway.get(gatewayName.toLowerCase());
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported payment gateway: " + gatewayName);
        }
        return processor;
    }
}
