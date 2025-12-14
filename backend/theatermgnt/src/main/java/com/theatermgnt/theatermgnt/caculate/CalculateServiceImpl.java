package com.theatermgnt.theatermgnt.caculate;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.customer.service.CustomerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CalculateServiceImpl implements CalculateService {

    CustomerService customerService;

    BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.8);

    @Override
    public BigDecimal discountPrice(BigDecimal originalPrice, String usernameOrEmail) {

        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_PRICE);
        }

        customerService.isCustomer(usernameOrEmail);

        BigDecimal discountedPrice = originalPrice.multiply(DISCOUNT_RATE);

        return discountedPrice.setScale(0, RoundingMode.HALF_UP);
    }
}
