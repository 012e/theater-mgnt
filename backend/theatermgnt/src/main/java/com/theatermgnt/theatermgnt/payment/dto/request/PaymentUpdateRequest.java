package com.theatermgnt.theatermgnt.payment.dto.request;

import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUpdateRequest {
    @NotNull(message = "INVALID_PAYMENT_STATUS")
    PaymentStatus status;

    String transactionId;
}

