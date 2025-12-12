package com.theatermgnt.theatermgnt.account.service;

import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.authentication.dto.request.OAuthCustomerCreationRequest;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerAccountCreationRequest;
import com.theatermgnt.theatermgnt.customer.dto.response.CustomerResponse;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffAccountCreationRequest;
import com.theatermgnt.theatermgnt.staff.dto.response.StaffResponse;

public interface RegistrationService {
    @Transactional
    CustomerResponse registerCustomerAccount(CustomerAccountCreationRequest request);

    @Transactional
    Account registerOAuthCustomer(OAuthCustomerCreationRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    StaffResponse registerStaffAccount(StaffAccountCreationRequest request);

    @Transactional
    StaffResponse createAdminAccount(StaffAccountCreationRequest request);

    StaffResponse internalCreateStaff(StaffAccountCreationRequest request, Set<Role> roles);
}
