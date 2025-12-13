package com.theatermgnt.theatermgnt.authentication.service;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.authentication.dto.request.AuthenticationRequest;
import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.entity.InvalidatedToken;
import com.theatermgnt.theatermgnt.authentication.repository.InvalidatedTokenRepository;
import com.theatermgnt.theatermgnt.authentication.repository.OtpTokenRepository;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Mock
    AccountRepository accountRepository;

    @Mock
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Mock
    OtpTokenRepository  otpTokenRepository;

    @Mock
    TokenService tokenService;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    public void setup() {
        authenticationService.SIGNER_KEY = "test-secret-key-test-secret-key";
        authenticationService.REFRESHABLE_DURATION = 1000 * 60 * 10;
        authenticationService.OTP_VALID_DURATION = 5;
    }

    @Test
    void authenticate_success() {
        //given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setLoginIdentifier("user1");
        request.setPassword("password");

        Account account = new Account();
        account.setPassword(new BCryptPasswordEncoder().encode("password"));

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(
                any(), any(), any())).thenReturn(Optional.of(account));

        when(tokenService.generateToken(account)).thenReturn("mock-token");

        //when
        AuthenticationResponse response = authenticationService.authenticate(request);

        //then
        assertTrue(response.isAuthenticated());
        assertEquals("mock-token", response.getToken());
    }

    @Test
    void authenticate_wrongPassword_throwException() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setLoginIdentifier("user1");
        request.setPassword("wrong-password");

        Account account = new Account();
        account.setPassword(new BCryptPasswordEncoder().encode("correct-password"));

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any())).thenReturn(Optional.of(account));

        AppException ex = assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    @Test
    void authenticate_userNotFound() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setLoginIdentifier("user1");
        request.setPassword("password");

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }
}
