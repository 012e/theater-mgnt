package com.theatermgnt.theatermgnt.payment.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreationRequest {
    // bookingId may be required by business rules; keep as optional for now
    String bookingId;

    @NotNull(message = "INVALID_PAYMENT_AMOUNT")
    BigDecimal amount;

    @NotBlank(message = "INVALID_PAYMENT_CURRENCY")
    String currency;

    String customerId;

    String transactionId;
}
