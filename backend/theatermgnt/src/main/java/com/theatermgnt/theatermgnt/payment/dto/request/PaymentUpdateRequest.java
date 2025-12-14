package com.theatermgnt.theatermgnt.payment.dto.request;

import jakarta.validation.constraints.NotNull;

import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUpdateRequest {
    @NotNull(message = "INVALID_PAYMENT_STATUS")
    PaymentStatus status;

    String transactionId;
}
