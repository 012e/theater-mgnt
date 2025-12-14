package com.theatermgnt.theatermgnt.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
class PaymentServiceImplTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PaymentMapper paymentMapper;

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
        sampleRequest.setCurrency("USD");
        sampleRequest.setTransactionId("tx-123");

        samplePayment = Payment.builder()
                .id("p-1")
                .amount(sampleRequest.getAmount())
                .currency(sampleRequest.getCurrency())
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .transactionId(sampleRequest.getTransactionId())
                .build();

        sampleResponse = new PaymentResponse();
        sampleResponse.setId(samplePayment.getId());
        sampleResponse.setAmount(samplePayment.getAmount());
        sampleResponse.setCurrency(samplePayment.getCurrency());
        sampleResponse.setMethod(samplePayment.getMethod());
        sampleResponse.setStatus(samplePayment.getStatus());
        sampleResponse.setTransactionId(samplePayment.getTransactionId());
    }

    // --- createPayment tests ---

    @Test
    void createPayment_whenTransactionIdExists_thenThrowsAppException() {
        when(paymentRepository.existsByTransactionId("tx-123")).thenReturn(true);

        assertThatThrownBy(() -> service.createPayment(sampleRequest))
                .isInstanceOf(AppException.class);

        verify(paymentRepository).existsByTransactionId("tx-123");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_happyPath_savesPendingAndReturnsResponse() {
        when(paymentRepository.existsByTransactionId("tx-123")).thenReturn(false);
        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        PaymentResponse resp = service.createPayment(sampleRequest);

        assertThat(resp).isNotNull();
        verify(paymentRepository).existsByTransactionId("tx-123");
        verify(paymentMapper).toPayment(sampleRequest);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        verify(paymentMapper).toPaymentResponse(saved);
    }

    @Test
    void createPayment_whenTransactionIdNull_doesNotCheckExists() {
        sampleRequest.setTransactionId(null);
        when(paymentMapper.toPayment(sampleRequest)).thenReturn(samplePayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(sampleResponse);

        PaymentResponse resp = service.createPayment(sampleRequest);

        assertThat(resp).isNotNull();
        verify(paymentRepository, never()).existsByTransactionId(any());
        verify(paymentRepository).save(any(Payment.class));
    }

    // --- getPayments tests ---

    @Test
    void getPayments_happyPath_mapsAll() {
        Payment p2 = Payment.builder().id("p-2").amount(BigDecimal.ONE).currency("USD").method(PaymentMethod.CASH)
                .status(PaymentStatus.COMPLETED).build();
        when(paymentRepository.findAll()).thenReturn(List.of(samplePayment, p2));
        when(paymentMapper.toPaymentResponse(samplePayment)).thenReturn(sampleResponse);
        when(paymentMapper.toPaymentResponse(p2)).thenReturn(new PaymentResponse("p-2", BigDecimal.ONE, "USD",
                PaymentMethod.CASH, PaymentStatus.COMPLETED, null, null, null, LocalDateTime.now(), null));

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
        when(paymentRepository.existsByTransactionId("tx-123")).thenReturn(false);
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
        assertThatThrownBy(() -> service.processCreditCardPayment(sampleRequest, null)).isInstanceOf(AppException.class);
    }

    @Test
    void processCreditCardPayment_happyPath_setsCreditCardAndCompleted() {
        CardDetails card = new CardDetails("4111111111111111", "John Doe", "12/25", "123");
        when(paymentRepository.existsByTransactionId("tx-123")).thenReturn(false);
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
        assertThatThrownBy(() -> service.processEwalletPayment(sampleRequest, null)).isInstanceOf(AppException.class);
    }

    @Test
    void processEwalletPayment_happyPath_setsWalletAndCompleted() {
        EwalletDetails wallet = new EwalletDetails("ew-1", "PAYPAL", "token");
        when(paymentRepository.existsByTransactionId("tx-123")).thenReturn(false);
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
        assertThatThrownBy(() -> service.processBankTransferPayment(sampleRequest, null)).isInstanceOf(AppException.class);
    }

    @Test
    void processBankTransferPayment_happyPath_setsBankTransferAndPending() {
        BankTransferDetails bank = new BankTransferDetails("12345678", "MyBank", "ref-1");
        when(paymentRepository.existsByTransactionId("tx-123")).thenReturn(false);
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

    // validateCommon indirectly via negative amounts

    @Test
    void processCreditCardPayment_whenAmountInvalid_thenThrows() {
        sampleRequest.setAmount(BigDecimal.ZERO);
        CardDetails card = new CardDetails("4111111111111111", "John Doe", "12/25", "123");
        assertThatThrownBy(() -> service.processCreditCardPayment(sampleRequest, card)).isInstanceOf(AppException.class);
    }

}

