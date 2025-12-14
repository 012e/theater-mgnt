package com.theatermgnt.theatermgnt.caculate;

import java.math.BigDecimal;

public interface CalculateService {
    public BigDecimal discountPrice(BigDecimal originalPrice, String usernameOrEmail);
}
