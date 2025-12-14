package com.theatermgnt.theatermgnt.payment.service;

import java.util.List;

import com.theatermgnt.theatermgnt.payment.dto.request.BankTransferDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.CardDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.EwalletDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.PaymentCreationRequest;
import com.theatermgnt.theatermgnt.payment.dto.request.PaymentUpdateRequest;
import com.theatermgnt.theatermgnt.payment.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createPayment(PaymentCreationRequest request);

    List<PaymentResponse> getPayments();

    PaymentResponse getPayment(String paymentId);

    void deletePayment(String paymentId);

    PaymentResponse updatePayment(String paymentId, PaymentUpdateRequest request);

    // New method-specific processing helpers
    PaymentResponse processCashPayment(PaymentCreationRequest request);

    PaymentResponse processCreditCardPayment(PaymentCreationRequest request, CardDetails cardDetails);

    PaymentResponse processEwalletPayment(PaymentCreationRequest request, EwalletDetails ewalletDetails);

    PaymentResponse processBankTransferPayment(PaymentCreationRequest request, BankTransferDetails bankDetails);
}
