package com.example.paymentservice.controller;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = PaymentResponse.from(service.create(idempotencyKey, request));
        return ResponseEntity.created(URI.create("/api/v1/payments/" + response.paymentId())).body(response);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable UUID id) {
        return PaymentResponse.from(service.get(id));
    }
}
