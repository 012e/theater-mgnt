package com.theatermgnt.theatermgnt.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.theatermgnt.theatermgnt.caculate.CalculateService;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.payment.dto.request.BankTransferDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.CardDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.EwalletDetails;
import com.theatermgnt.theatermgnt.payment.dto.request.PaymentCreationRequest;
import com.theatermgnt.theatermgnt.payment.dto.request.PaymentUpdateRequest;
import com.theatermgnt.theatermgnt.payment.dto.response.PaymentResponse;
import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.enums.PaymentMethod;
import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;
import com.theatermgnt.theatermgnt.payment.mapper.PaymentMapper;
import com.theatermgnt.theatermgnt.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceImplTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PaymentMapper paymentMapper;

    @Mock
    CalculateService calculateService;

    @InjectMocks
    PaymentServiceImpl service;

    @Captor
    ArgumentCaptor<Payment> paymentCaptor;

    private PaymentCreationRequest sampleRequest;
    private Payment samplePayment;
    private PaymentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = new PaymentCreationRequest();
        sampleRequest.setAmount(BigDecimal.valueOf(12.5));

        samplePayment = Payment.builder()
                .id("p-1")
                .amount(sampleRequest.getAmount())
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .build();

        sampleResponse = new PaymentResponse();
        sampleResponse.setId(samplePayment.getId());
        sampleResponse.setAmount(samplePayment.getAmount());
        sampleResponse.setMethod(samplePayment.getMethod());
        sampleResponse.setStatus(samplePayment.getStatus());
    }

    // --- createPayment tests ---

    // --- getPayments tests ---
    @Test
    void getPayments_happyPath_mapsAll() {
        Payment p2 = Payment.builder()
                .id("p-2")
                .amount(BigDecimal.ONE)
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED)
                .build();
        when(paymentRepository.findAll()).thenReturn(List.of(samplePayment, p2));
        when(paymentMapper.toPaymentResponse(samplePayment)).thenReturn(sampleResponse);
        when(paymentMapper.toPaymentResponse(p2))
                .thenReturn(new PaymentResponse(
                        "p-2",
                        BigDecimal.ONE,
                        "USD",
                        PaymentMethod.CASH,
                        PaymentStatus.COMPLETED,
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        null));

        List<PaymentResponse> results = service.getPayments();

        assertThat(results).hasSize(2);
        verify(paymentRepository).findAll();
        verify(paymentMapper).toPaymentResponse(samplePayment);
        verify(paymentMapper).toPaymentResponse(p2);
    }

    @Test
    void getPayments_emptyList_returnsEmpty() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        List<PaymentResponse> results = service.getPayments();

        assertThat(results).isEmpty();
        verify(paymentRepository).findAll();
    }

    // --- getPayment tests ---

    @Test
    void getPayment_whenNotFound_thenThrows() {
        when(paymentRepository.findById("p-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPayment("p-1")).isInstanceOf(AppException.class);

        verify(paymentRepository).findById("p-1");
    }

    @Test
    void getPayment_happyPath_returnsResponse() {
        when(paymentRepository.findById("p-1")).thenReturn(Optional.of(samplePayment));
        when(paymentMapper.toPaymentResponse(samplePayment)).thenReturn(sampleResponse);

        PaymentResponse resp = service.getPayment("p-1");

        assertThat(resp).isNotNull();
        verify(paymentRepository).findById("p-1");
        verify(paymentMapper).toPaymentResponse(samplePayment);
    }

    // --- deletePayment tests ---

    @Test
    void deletePayment_invokesRepository() {
        doNothing().when(paymentRepository).deleteById("p-1");

        service.deletePayment("p-1");

        verify(paymentRepository).deleteById("p-1");
    }

    // --- updatePayment tests ---

    @Test
    void updatePayment_whenNotFound_thenThrows() {
        PaymentUpdateRequest updateReq = new PaymentUpdateRequest();
        updateReq.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById("p-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePayment("p-1", updateReq)).isInstanceOf(AppException.class);

        verify(paymentRepository).findById("p-1");
    }

    @Test
    void updatePayment_happyPath_updatesAndSaves() {
        PaymentUpdateRequest updateReq = new PaymentUpdateRequest();
        updateReq.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById("p-1")).thenReturn(Optional.of(samplePayment));
        doNothing().when(paymentMapper).updatePayment(samplePayment, updateReq);
        when(paymentRepository.save(samplePayment)).thenReturn(samplePayment);
        when(paymentMapper.toPaymentResponse(samplePayment)).thenReturn(sampleResponse);

        PaymentResponse resp = service.updatePayment("p-1", updateReq);

        assertThat(resp).isNotNull();
        verify(paymentRepository).findById("p-1");
        verify(paymentMapper).updatePayment(samplePayment, updateReq);
        verify(paymentRepository).save(samplePayment);
        verify(paymentMapper).toPaymentResponse(samplePayment);
    }

    // --- processCashPayment ---

    @Test
    void processCashPayment_happyPath_setsCashAndCompleted() {
        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        PaymentResponse resp = service.processCashPayment(sampleRequest);

        assertThat(resp).isNotNull();
        verify(paymentMapper).toPayment(sampleRequest);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    // --- processCreditCardPayment ---

    @Test
    void processCreditCardPayment_whenCardDetailsNull_thenThrows() {
        assertThatThrownBy(() -> service.processCreditCardPayment(sampleRequest, null))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processCreditCardPayment_whenCardDetailsInvalid_thenThrows() {
        // Any invalid card should throw - testing one example covers the validation logic
        CardDetails invalidCard = new CardDetails("invalid", "John Doe", "12/30", "123");
        assertThatThrownBy(() -> service.processCreditCardPayment(sampleRequest, invalidCard))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processCreditCardPayment_happyPath_setsCreditCardAndCompleted() {
        CardDetails card = new CardDetails("4111111111111111", "John Doe", "12/30", "123");
        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        PaymentResponse resp = service.processCreditCardPayment(sampleRequest, card);

        assertThat(resp).isNotNull();
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    // --- processEwalletPayment ---

    @Test
    void processEwalletPayment_whenEwalletDetailsNull_thenThrows() {
        assertThatThrownBy(() -> service.processEwalletPayment(sampleRequest, null))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processEwalletPayment_whenEwalletDetailsInvalid_thenThrows() {
        // Any invalid wallet should throw - testing one example covers the validation logic
        EwalletDetails invalidWallet = new EwalletDetails(null, "PAYPAL", "token");
        assertThatThrownBy(() -> service.processEwalletPayment(sampleRequest, invalidWallet))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processEwalletPayment_happyPath_setsWalletAndCompleted() {
        EwalletDetails wallet = new EwalletDetails("ew-1", "PAYPAL", "token");
        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        PaymentResponse resp = service.processEwalletPayment(sampleRequest, wallet);

        assertThat(resp).isNotNull();
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.WALLET);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    // --- processBankTransferPayment ---

    @Test
    void processBankTransferPayment_whenBankDetailsNull_thenThrows() {
        assertThatThrownBy(() -> service.processBankTransferPayment(sampleRequest, null))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processBankTransferPayment_whenBankDetailsInvalid_thenThrows() {
        // Any invalid bank details should throw - testing one example covers the validation logic
        BankTransferDetails invalidBank = new BankTransferDetails(null, "MyBank", "ref-1");
        assertThatThrownBy(() -> service.processBankTransferPayment(sampleRequest, invalidBank))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processBankTransferPayment_happyPath_setsBankTransferAndPending() {
        BankTransferDetails bank = new BankTransferDetails("12345678", "MyBank", "ref-1");
        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        PaymentResponse resp = service.processBankTransferPayment(sampleRequest, bank);

        assertThat(resp).isNotNull();
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    // --- validateCommon tests ---

    @Test
    void processCreditCardPayment_whenRequestNull_thenThrows() {
        CardDetails card = new CardDetails("4111111111111111", "John Doe", "12/30", "123");
        assertThatThrownBy(() -> service.processCreditCardPayment(null, card)).isInstanceOf(AppException.class);
    }

    @Test
    void processCreditCardPayment_whenRequestInvalid_thenThrows() {
        // Any invalid request should throw - testing one example covers the validation logic
        sampleRequest.setAmount(BigDecimal.ZERO);
        CardDetails card = new CardDetails("4111111111111111", "John Doe", "12/30", "123");
        assertThatThrownBy(() -> service.processCreditCardPayment(sampleRequest, card))
                .isInstanceOf(AppException.class);
    }

    // --- Payment Amount Validation Tests ---

    @Test
    void processCashPayment_whenAmountSufficient_thenSucceeds() {
        // Given: Payment with originalPrice and customerId for discount validation
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setAmount(BigDecimal.valueOf(80)); // After 20% discount
        sampleRequest.setCustomerId("customer-1");

        // Mock: validatePaymentAmountSufficient should not throw
        doNothing()
                .when(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());

        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        // When & Then: Should complete successfully
        PaymentResponse resp = service.processCashPayment(sampleRequest);

        assertThat(resp).isNotNull();
        verify(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());
    }

    @Test
    void processCashPayment_whenAmountInsufficient_thenThrows() {
        // Given: Payment with insufficient amount
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setAmount(BigDecimal.valueOf(50)); // Too low, even with discount
        sampleRequest.setCustomerId("customer-1");

        // Mock: validatePaymentAmountSufficient throws exception
        doThrow(new AppException(com.theatermgnt.theatermgnt.common.exception.ErrorCode.INSUFFICIENT_PAYMENT_AMOUNT))
                .when(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());

        // When & Then: Should throw AppException
        assertThatThrownBy(() -> service.processCashPayment(sampleRequest)).isInstanceOf(AppException.class);

        verify(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());
    }

    @Test
    void processCreditCardPayment_whenAmountSufficient_thenSucceeds() {
        // Given: Payment with valid amount after discount
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setAmount(BigDecimal.valueOf(90));
        sampleRequest.setCustomerId("customer-1");
        CardDetails card = new CardDetails("4111111111111111", "John Doe", "12/30", "123");

        doNothing()
                .when(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());

        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        // When & Then: Should succeed
        PaymentResponse resp = service.processCreditCardPayment(sampleRequest, card);

        assertThat(resp).isNotNull();
        verify(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());
    }

    @Test
    void processCreditCardPayment_whenAmountInsufficient_thenThrows() {
        // Given: Payment with insufficient amount
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setAmount(BigDecimal.valueOf(40));
        sampleRequest.setCustomerId("customer-1");
        CardDetails card = new CardDetails("4111111111111111", "John Doe", "12/30", "123");

        doThrow(new AppException(com.theatermgnt.theatermgnt.common.exception.ErrorCode.INSUFFICIENT_PAYMENT_AMOUNT))
                .when(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());

        // When & Then: Should throw
        assertThatThrownBy(() -> service.processCreditCardPayment(sampleRequest, card))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processEwalletPayment_whenAmountSufficient_thenSucceeds() {
        // Given: Valid ewallet payment with sufficient amount
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setAmount(BigDecimal.valueOf(85));
        sampleRequest.setCustomerId("customer-1");
        EwalletDetails wallet = new EwalletDetails("ew-1", "PAYPAL", "token");

        doNothing()
                .when(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());

        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        // When & Then: Should succeed
        PaymentResponse resp = service.processEwalletPayment(sampleRequest, wallet);

        assertThat(resp).isNotNull();
        verify(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());
    }

    @Test
    void processEwalletPayment_whenAmountInsufficient_thenThrows() {
        // Given: Insufficient amount
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setAmount(BigDecimal.valueOf(30));
        sampleRequest.setCustomerId("customer-1");
        EwalletDetails wallet = new EwalletDetails("ew-1", "PAYPAL", "token");

        doThrow(new AppException(com.theatermgnt.theatermgnt.common.exception.ErrorCode.INSUFFICIENT_PAYMENT_AMOUNT))
                .when(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());

        // When & Then: Should throw
        assertThatThrownBy(() -> service.processEwalletPayment(sampleRequest, wallet))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processBankTransferPayment_whenAmountSufficient_thenSucceeds() {
        // Given: Valid bank transfer with sufficient amount
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setAmount(BigDecimal.valueOf(95));
        sampleRequest.setCustomerId("customer-1");
        BankTransferDetails bank = new BankTransferDetails("12345678", "MyBank", "ref-1");

        doNothing()
                .when(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());

        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        // When & Then: Should succeed
        PaymentResponse resp = service.processBankTransferPayment(sampleRequest, bank);

        assertThat(resp).isNotNull();
        verify(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());
    }

    @Test
    void processBankTransferPayment_whenAmountInsufficient_thenThrows() {
        // Given: Insufficient amount
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setAmount(BigDecimal.valueOf(20));
        sampleRequest.setCustomerId("customer-1");
        BankTransferDetails bank = new BankTransferDetails("12345678", "MyBank", "ref-1");

        doThrow(new AppException(com.theatermgnt.theatermgnt.common.exception.ErrorCode.INSUFFICIENT_PAYMENT_AMOUNT))
                .when(calculateService)
                .validatePaymentAmountSufficient(
                        sampleRequest.getOriginalPrice(), sampleRequest.getAmount(), sampleRequest.getCustomerId());

        // When & Then: Should throw
        assertThatThrownBy(() -> service.processBankTransferPayment(sampleRequest, bank))
                .isInstanceOf(AppException.class);
    }

    @Test
    void processCashPayment_whenOriginalPriceNull_skipsValidation() {
        // Given: Payment without originalPrice (no discount validation needed)
        sampleRequest.setOriginalPrice(null);
        sampleRequest.setCustomerId("customer-1");

        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        // When: Process payment
        PaymentResponse resp = service.processCashPayment(sampleRequest);

        // Then: Should succeed without calling calculateService
        assertThat(resp).isNotNull();
        verify(calculateService, org.mockito.Mockito.never()).validatePaymentAmountSufficient(any(), any(), any());
    }

    @Test
    void processCashPayment_whenCustomerIdNull_skipsValidation() {
        // Given: Payment without customerId (no discount validation needed)
        sampleRequest.setOriginalPrice(BigDecimal.valueOf(100));
        sampleRequest.setCustomerId(null);

        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        // When: Process payment
        PaymentResponse resp = service.processCashPayment(sampleRequest);

        // Then: Should succeed without calling calculateService
        assertThat(resp).isNotNull();
        verify(calculateService, org.mockito.Mockito.never()).validatePaymentAmountSufficient(any(), any(), any());
    }
}
