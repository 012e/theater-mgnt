package com.theatermgnt.theatermgnt.payment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.payment.dto.request.PaymentCreationRequest;
import com.theatermgnt.theatermgnt.payment.dto.request.PaymentUpdateRequest;
import com.theatermgnt.theatermgnt.payment.dto.request.CardDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.EwalletDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.BankTransferDetails;
import com.theatermgnt.theatermgnt.payment.dto.response.PaymentResponse;
import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.mapper.PaymentMapper;
import com.theatermgnt.theatermgnt.payment.repository.PaymentRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceImpl implements PaymentService {

    PaymentRepository paymentRepository;

    PaymentMapper paymentMapper;

    @Override
    public PaymentResponse createPayment(PaymentCreationRequest request) {
        if (request.getTransactionId() != null && paymentRepository.existsByTransactionId(request.getTransactionId())) {
            throw new AppException(ErrorCode.valueOf("CINEMA_EXISTED"));
        }

        Payment payment = paymentMapper.toPayment(request);
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentResponse> getPayments() {
        return paymentRepository.findAll().stream().map(paymentMapper::toPaymentResponse).toList();
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        return paymentMapper.toPaymentResponse(paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.valueOf("CINEMA_NOT_EXISTED"))));
    }

    @Override
    public void deletePayment(String paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    @Override
    public PaymentResponse updatePayment(String paymentId, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.valueOf("CINEMA_NOT_EXISTED")));

        paymentMapper.updatePayment(payment, request);

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // New method-specific processors
    @Override
    public PaymentResponse processCashPayment(PaymentCreationRequest request) {
        validateCommon(request);
        // For cash, mark completed immediately (business rule could vary)
        Payment payment = paymentMapper.toPayment(request);
        payment.setMethod(com.theatermgnt.theatermgnt.payment.enums.PaymentMethod.CASH);
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse processCreditCardPayment(PaymentCreationRequest request, CardDetails cardDetails) {
        validateCommon(request);
        if (cardDetails == null) {
            throw new AppException(ErrorCode.valueOf("INVALID_REQUEST"));
        }

        // Basic mask and validation could be applied here. Real gateway call omitted.
        Payment payment = paymentMapper.toPayment(request);
        payment.setMethod(com.theatermgnt.theatermgnt.payment.enums.PaymentMethod.CREDIT_CARD);
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        // Simulate gateway success
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.COMPLETED);
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse processEwalletPayment(PaymentCreationRequest request, EwalletDetails ewalletDetails) {
        validateCommon(request);
        if (ewalletDetails == null) {
            throw new AppException(ErrorCode.valueOf("INVALID_REQUEST"));
        }

        Payment payment = paymentMapper.toPayment(request);
        payment.setMethod(com.theatermgnt.theatermgnt.payment.enums.PaymentMethod.WALLET);
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        // Simulate immediate success for ewallet
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.COMPLETED);
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse processBankTransferPayment(PaymentCreationRequest request, BankTransferDetails bankDetails) {
        validateCommon(request);
        if (bankDetails == null) {
            throw new AppException(ErrorCode.valueOf("INVALID_REQUEST"));
        }

        Payment payment = paymentMapper.toPayment(request);
        payment.setMethod(com.theatermgnt.theatermgnt.payment.enums.PaymentMethod.BANK_TRANSFER);
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        // Bank transfer may be manual/async — keep as pending
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // Common validations
    private void validateCommon(PaymentCreationRequest request) {
        if (request.getTransactionId() != null && paymentRepository.existsByTransactionId(request.getTransactionId())) {
            throw new AppException(ErrorCode.valueOf("CINEMA_EXISTED"));
        }
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new AppException(ErrorCode.valueOf("INVALID_PAYMENT_AMOUNT"));
        }
    }

}
