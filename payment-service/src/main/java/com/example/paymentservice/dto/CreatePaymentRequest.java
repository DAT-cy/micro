package com.example.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank @Size(max = 100) String orderId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency) {
}
