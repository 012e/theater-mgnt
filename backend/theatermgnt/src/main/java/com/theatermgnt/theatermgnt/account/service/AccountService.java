package com.theatermgnt.theatermgnt.account.service;

import com.theatermgnt.theatermgnt.account.dto.request.BaseAccountCreationRequest;
import com.theatermgnt.theatermgnt.account.dto.request.IAccountUpdateRequest;
import com.theatermgnt.theatermgnt.account.dto.request.PasswordCreationRequest;
import com.theatermgnt.theatermgnt.account.entity.Account;

public interface AccountService {
    Account createAccount(BaseAccountCreationRequest request);

    void updateAccount(Account account, IAccountUpdateRequest request);

    void createPassword(PasswordCreationRequest request);
}
