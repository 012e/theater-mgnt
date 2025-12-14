package com.theatermgnt.theatermgnt.payment.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.payment.dto.request.BankTransferDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.CardDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.EwalletDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.PaymentCreationRequest;
import com.theatermgnt.theatermgnt.payment.dto.request.PaymentUpdateRequest;
import com.theatermgnt.theatermgnt.payment.dto.response.PaymentResponse;
import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.mapper.PaymentMapper;
import com.theatermgnt.theatermgnt.payment.repository.PaymentRepository;
import com.theatermgnt.theatermgnt.caculate.CalculateService;

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

    CalculateService calculateService;

    @Override
    public PaymentResponse createPayment(PaymentCreationRequest request) {
        validateCommon(request);
        Payment payment = paymentMapper.toPayment(request);
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentResponse> getPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        return paymentMapper.toPaymentResponse(paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTED)));
    }

    @Override
    public void deletePayment(String paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    @Override
    public PaymentResponse updatePayment(String paymentId, PaymentUpdateRequest request) {
        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXISTED));

        paymentMapper.updatePayment(payment, request);

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // New method-specific processors
    @Override
    public PaymentResponse processCashPayment(PaymentCreationRequest request) {
        validateCommon(request);
        validatePaymentAmount(request);
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
        validatePaymentAmount(request);
        validateCardDetails(cardDetails);

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
        validatePaymentAmount(request);
        validateEwalletDetails(ewalletDetails);

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
        validatePaymentAmount(request);
        validateBankTransferDetails(bankDetails);

        Payment payment = paymentMapper.toPayment(request);
        payment.setMethod(com.theatermgnt.theatermgnt.payment.enums.PaymentMethod.BANK_TRANSFER);
        payment.setStatus(com.theatermgnt.theatermgnt.payment.enums.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        // Bank transfer may be manual/async — keep as pending
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // Common validations
    private void validateCommon(PaymentCreationRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    private void validateCardDetails(CardDetails cardDetails) {
        if (cardDetails == null) {
            throw new AppException(ErrorCode.INVALID_CARD_DETAILS);
        }

        // Validate card number (consolidated checks)
        validateAndCleanCardNumber(cardDetails.getCardNumber());

        // Validate other required fields
        validateNotEmpty(cardDetails.getCardHolderName(), ErrorCode.INVALID_CARD_HOLDER);
        validateCardExpiry(cardDetails.getExpiry());
        validateCardCvv(cardDetails.getCvv());
    }

    private void validateEwalletDetails(EwalletDetails ewalletDetails) {
        if (ewalletDetails == null) {
            throw new AppException(ErrorCode.INVALID_EWALLET_DETAILS);
        }

        validateNotEmpty(ewalletDetails.getWalletId(), ErrorCode.INVALID_EWALLET_ID);

        // Optional provider validation
        if (ewalletDetails.getProvider() != null
                && !ewalletDetails.getProvider().trim().isEmpty()) {
            String provider = ewalletDetails.getProvider().toUpperCase();
            if (!List.of("PAYPAL", "MOMO", "ZALOPAY", "VNPAY", "GOOGLEPAY", "APPLEPAY")
                    .contains(provider)) {
                throw new AppException(ErrorCode.INVALID_EWALLET_PROVIDER);
            }
        }
    }

    private void validateBankTransferDetails(BankTransferDetails bankDetails) {
        if (bankDetails == null) {
            throw new AppException(ErrorCode.INVALID_BANK_DETAILS);
        }

        validateNotEmpty(bankDetails.getAccountNumber(), ErrorCode.INVALID_BANK_ACCOUNT);
        validateNotEmpty(bankDetails.getBankName(), ErrorCode.INVALID_BANK_NAME);
    }

    // Utility validation methods
    private void validateNotEmpty(String value, ErrorCode errorCode) {
        if (value == null || value.trim().isEmpty()) {
            throw new AppException(errorCode);
        }
    }

    private String validateAndCleanCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_CARD_NUMBER);
        }

        String cleaned = cardNumber.replaceAll("\\s+", "");
        if (cleaned.length() < 12 || cleaned.length() > 19 || !isValidCardNumberLuhn(cleaned)) {
            throw new AppException(ErrorCode.INVALID_CARD_NUMBER);
        }

        return cleaned;
    }

    private void validateCardExpiry(String expiry) {
        if (expiry == null || expiry.trim().isEmpty() || !expiry.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            throw new AppException(ErrorCode.INVALID_CARD_EXPIRY);
        }

        if (!isCardNotExpired(expiry)) {
            throw new AppException(ErrorCode.CARD_EXPIRED);
        }
    }

    private void validateCardCvv(String cvv) {
        if (cvv == null || cvv.trim().isEmpty() || !cvv.matches("^[0-9]{3,4}$")) {
            throw new AppException(ErrorCode.INVALID_CARD_CVV);
        }
    }

    /**
     * Luhn algorithm to validate card number
     */
    private boolean isValidCardNumberLuhn(String cardNumber) {
        if (!cardNumber.matches("^[0-9]+$")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Check if the card expiry date is in the future
     */
    private boolean isCardNotExpired(String expiry) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiryDate = YearMonth.parse(expiry, formatter);
            YearMonth currentDate = YearMonth.now();
            return !expiryDate.isBefore(currentDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validate that the transferred payment amount is sufficient after applying discounts
     * @param request The payment creation request containing amount, customerId, and originalPrice
     * @throws AppException if the transferred amount is insufficient
     */
    private void validatePaymentAmount(PaymentCreationRequest request) {
        // If originalPrice is not provided, skip discount validation
        if (request.getOriginalPrice() == null || request.getCustomerId() == null) {
            return;
        }

        // Use CalculateService to validate if transferred amount is sufficient
        calculateService.validatePaymentAmountSufficient(
            request.getOriginalPrice(),
            request.getAmount(),
            request.getCustomerId()
        );
    }
}
