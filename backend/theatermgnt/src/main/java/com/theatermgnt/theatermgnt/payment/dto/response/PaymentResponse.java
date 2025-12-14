package com.theatermgnt.theatermgnt.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.theatermgnt.theatermgnt.payment.enums.PaymentMethod;
import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    String id;
    BigDecimal amount;
    String currency;
    PaymentMethod method;
    PaymentStatus status;
    String transactionId;
    String bookingId;
    String customerId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
