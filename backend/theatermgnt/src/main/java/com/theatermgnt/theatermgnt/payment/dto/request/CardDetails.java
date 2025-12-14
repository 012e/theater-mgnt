package com.theatermgnt.theatermgnt.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDetails {
    @NotBlank(message = "INVALID_CARD_NUMBER")
    @Size(min = 12, max = 19)
    String cardNumber;

    @NotBlank(message = "INVALID_CARD_HOLDER")
    String cardHolderName;

    @NotBlank(message = "INVALID_CARD_EXPIRY")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "INVALID_CARD_EXPIRY_FORMAT")
    String expiry; // MM/yy

    @NotBlank(message = "INVALID_CARD_CVV")
    @Size(min = 3, max = 4)
    String cvv;
}
