package com.theatermgnt.theatermgnt.customer.service;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerAccountCreationRequest;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerProfileUpdateRequest;
import com.theatermgnt.theatermgnt.customer.dto.response.CustomerResponse;
import com.theatermgnt.theatermgnt.customer.entity.Customer;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CustomerService {
    @Transactional
    Customer createCustomerProfile(CustomerAccountCreationRequest request, Account account);

    CustomerResponse getCustomerProfileById(String customerId);

    CustomerResponse getMyInfo();

    List<CustomerResponse> getAll();

    @Transactional
    CustomerResponse updateCustomerProfile(String customerId, CustomerProfileUpdateRequest request);
}
