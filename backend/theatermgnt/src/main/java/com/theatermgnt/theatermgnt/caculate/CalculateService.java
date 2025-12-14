package com.theatermgnt.theatermgnt.caculate;

import com.theatermgnt.theatermgnt.common.exception.AppException;

import java.math.BigDecimal;

public interface CalculateService {
    public BigDecimal discountPrice(BigDecimal originalPrice, String usernameOrEmail);

    /**
     * Calculate the required amount after discount and validate if the transferred amount is sufficient
     * @param originalPrice The original price before discount
     * @param transferredAmount The amount that was transferred/paid
     * @param usernameOrEmail The customer's username or email for discount eligibility
     * @return true if the transferred amount is sufficient, false otherwise
     */
    public boolean isPaymentAmountSufficient(BigDecimal originalPrice, BigDecimal transferredAmount, String usernameOrEmail);

    /**
     * Validate that the transferred amount is sufficient, throws exception if not
     * @param originalPrice The original price before discount
     * @param transferredAmount The amount that was transferred/paid
     * @param usernameOrEmail The customer's username or email for discount eligibility
     * @throws AppException if the transferred amount is insufficient
     */
    public void validatePaymentAmountSufficient(BigDecimal originalPrice, BigDecimal transferredAmount, String usernameOrEmail);
}
