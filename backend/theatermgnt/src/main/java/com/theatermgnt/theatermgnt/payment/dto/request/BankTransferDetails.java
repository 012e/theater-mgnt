package com.theatermgnt.theatermgnt.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankTransferDetails {
    @NotBlank(message = "INVALID_BANK_ACCOUNT")
    String accountNumber;

    @NotBlank(message = "INVALID_BANK_NAME")
    String bankName;

    String reference;
}

