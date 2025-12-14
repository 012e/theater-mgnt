package com.theatermgnt.theatermgnt.customer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.customer.mapper.CustomerMapper;
import com.theatermgnt.theatermgnt.customer.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @InjectMocks
    CustomerServiceImpl customerService;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    AccountRepository accountRepository;

    @Mock
    CustomerMapper customerMapper;

    // ===== helper =====
    private Account sampleAccount(String id, String username) {
        return Account.builder()
                .id(id)
                .username(username)
                .email(username + "@gmail.com")
                .build();
    }

    // TC-01 — B: input empty: INVALID_IDENTIFIER
    @Test
    void isCustomer_whenInputEmpty_thenThrowInvalidIdentifier() {

        AppException ex = assertThrows(AppException.class, () -> customerService.isCustomer(""));

        assertEquals(ErrorCode.INVALID_IDENTIFIER, ex.getErrorCode());

        verifyNoInteractions(accountRepository, customerRepository);
    }

    // TC-02 — A: account not found: ACCOUNT_NOT_FOUND
    @Test
    void isCustomer_whenAccountNotFound_thenThrowAccountNotFound() {

        String email = "notexist@gmail.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> customerService.isCustomer(email));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());

        verify(accountRepository).findByEmail(email);
        verifyNoInteractions(customerRepository);
    }

    // TC-03 — A: account exists but NOT customer: CUSTOMER_NOT_FOUND
    @Test
    void isCustomer_whenAccountExistsButNotCustomer_thenThrowCustomerNotFound() {

        Account account = sampleAccount("acc-1", "user01");
        when(accountRepository.findByUsername("user01")).thenReturn(Optional.of(account));
        when(customerRepository.existsByAccountId(account.getId())).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> customerService.isCustomer("user01"));

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, ex.getErrorCode());

        verify(accountRepository).findByUsername("user01");
        verify(customerRepository).existsByAccountId(account.getId());
    }

    // TC-04 — N: account: customer exist → return true
    @Test
    void isCustomer_whenCustomer01Exists_thenReturnTrue() {

        Account account = sampleAccount("acc-2", "customer01");
        when(accountRepository.findByUsername("customer01")).thenReturn(Optional.of(account));
        when(customerRepository.existsByAccountId(account.getId())).thenReturn(true);

        boolean result = customerService.isCustomer("customer01");

        assertTrue(result);

        verify(accountRepository).findByUsername("customer01");
        verify(customerRepository).existsByAccountId(account.getId());
    }

    // TC-05 — A: account not found: ACCOUNT_NOT_FOUND
    @Test
    void isCustomer_whenAccountNotFound_byUsername_thenThrowAccountNotFound() {

        String username = "notUser01";
        when(accountRepository.findByUsername(username)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> customerService.isCustomer(username));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());

        verify(accountRepository).findByUsername(username);
        verifyNoInteractions(customerRepository);
    }

    // TC-06 — N: account: customer exist → return true
    @Test
    void isCustomer_whenEmailExistsAndIsCustomer_thenReturnTrue() {

        String email = "customer02@gmail.com";
        Account account = sampleAccount("acc-3", "customer02");

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(customerRepository.existsByAccountId(account.getId())).thenReturn(true);

        boolean result = customerService.isCustomer(email);

        assertTrue(result);

        verify(accountRepository).findByEmail(email);
        verify(customerRepository).existsByAccountId(account.getId());
    }
}
