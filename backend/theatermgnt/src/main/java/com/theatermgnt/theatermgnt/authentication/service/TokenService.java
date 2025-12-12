package com.theatermgnt.theatermgnt.authentication.service;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.staff.entity.Staff;

public interface TokenService {
    String generateToken(Account account);

    String buildScope(Staff staff);
}
