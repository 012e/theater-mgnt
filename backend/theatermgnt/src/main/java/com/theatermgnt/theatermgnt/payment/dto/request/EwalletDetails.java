package com.theatermgnt.theatermgnt.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EwalletDetails {
    @NotBlank(message = "INVALID_EWALLET_ID")
    String walletId;

    String provider; // e.g. PAYPAL, GSR

    String authToken;
}
