package com.theatermgnt.theatermgnt.payment.dto.request;

import java.math.BigDecimal;

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

    String customerId;

    // Original price before discount (for validation purposes)
    BigDecimal originalPrice;
}
