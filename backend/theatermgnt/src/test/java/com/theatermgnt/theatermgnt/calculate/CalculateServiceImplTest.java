package com.theatermgnt.theatermgnt.calculate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.caculate.CalculateServiceImpl;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.customer.service.CustomerService;

@ExtendWith(MockitoExtension.class)
public class CalculateServiceImplTest {
    @InjectMocks
    CalculateServiceImpl calculateService;

    @Mock
    CustomerService customerService;

    // TC-01 — B: originalPrice null: INVALID_PRICE
    @Test
    void discountPrice_whenPriceIsNull_thenThrowInvalidPrice() {

        AppException ex = assertThrows(AppException.class, () -> calculateService.discountPrice(null, "customer01"));

        assertEquals(ErrorCode.INVALID_PRICE, ex.getErrorCode());
        verifyNoInteractions(customerService);
    }

    // TC-02 — A: isCustomer throws exception
    @Test
    void discountPrice_whenIsCustomerThrows_thenPropagateException() {

        BigDecimal price = BigDecimal.valueOf(100_000);

        doThrow(new AppException(ErrorCode.CUSTOMER_NOT_FOUND))
                .when(customerService)
                .isCustomer("user01");

        AppException ex = assertThrows(AppException.class, () -> calculateService.discountPrice(price, "user01"));

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, ex.getErrorCode());

        verify(customerService).isCustomer("user01");
    }

    // TC-03 — N: customer hợp lệ: giảm 20%
    @Test
    void discountPrice_whenCustomerValid_thenReturnDiscountedPrice() {

        BigDecimal price = BigDecimal.valueOf(100_000);

        when(customerService.isCustomer("customer01")).thenReturn(true);

        BigDecimal result = calculateService.discountPrice(price, "customer01");

        assertEquals(BigDecimal.valueOf(80_000).setScale(0, RoundingMode.HALF_UP), result);

        verify(customerService).isCustomer("customer01");
    }

    // TC-04 — B: kiểm tra rounding HALF_UP
    @Test
    void discountPrice_whenHasDecimal_thenRoundHalfUp() {

        BigDecimal price = BigDecimal.valueOf(99_999); // * 0.8 = 79_999.2

        when(customerService.isCustomer("customer01")).thenReturn(true);

        BigDecimal result = calculateService.discountPrice(price, "customer01");

        assertEquals(BigDecimal.valueOf(79_999), result);

        verify(customerService).isCustomer("customer01");
    }

    // TC-DP-05 — B: originalPrice = -1: INVALID_PRICE
    @Test
    void discountPrice_whenPriceIsNegative_thenThrowInvalidPrice() {

        BigDecimal negativePrice = BigDecimal.valueOf(-1);

        AppException ex =
                assertThrows(AppException.class, () -> calculateService.discountPrice(negativePrice, "customer01"));

        assertEquals(ErrorCode.INVALID_PRICE, ex.getErrorCode());

        verifyNoInteractions(customerService);
    }
}
