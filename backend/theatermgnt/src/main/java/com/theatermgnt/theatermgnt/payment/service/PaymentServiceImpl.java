package com.theatermgnt.theatermgnt.payment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.payment.dto.request.PaymentCreationRequest;
import com.theatermgnt.theatermgnt.payment.dto.request.PaymentUpdateRequest;
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

}

