package com.theatermgnt.theatermgnt.authentication.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import com.theatermgnt.theatermgnt.authentication.dto.request.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.entity.InvalidatedToken;
import com.theatermgnt.theatermgnt.authentication.entity.OtpToken;
import com.theatermgnt.theatermgnt.authentication.event.PasswordResetEvent;
import com.theatermgnt.theatermgnt.authentication.repository.InvalidatedTokenRepository;
import com.theatermgnt.theatermgnt.authentication.repository.OtpTokenRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
    @Spy
    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Mock
    AccountRepository accountRepository;

    @Mock
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Mock
    OtpTokenRepository otpTokenRepository;

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
        // given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setLoginIdentifier("user1");
        request.setPassword("password");

        Account account = new Account();
        account.setPassword(new BCryptPasswordEncoder().encode("password"));

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.of(account));

        when(tokenService.generateToken(account)).thenReturn("mock-token");

        // when
        AuthenticationResponse response = authenticationService.authenticate(request);

        // then
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

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.of(account));

        AppException ex = assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    @Test
    void authenticate_userNotFound() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setLoginIdentifier("user1");
        request.setPassword("password");

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }

    @Test
    void logout_success() throws ParseException, JOSEException {
        // given
        LogoutRequest request = new LogoutRequest();
        request.setToken("valid-token");

        Date expiryTime = new Date(System.currentTimeMillis() + 60_000);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID("jti-123")
                .expirationTime(expiryTime)
                .build();

        SignedJWT signedJWT = mock(SignedJWT.class);

        when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        // mock verifyToken
        doReturn(signedJWT).when(authenticationService).verifyToken("valid-token", true);

        // when
        authenticationService.logout(request);

        // then
        ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(invalidatedTokenRepository).save(captor.capture());

        InvalidatedToken saved = captor.getValue();
        assertEquals("jti-123", saved.getId());
        assertEquals(expiryTime, saved.getExpiryTime());
    }

    @Test
    void logout_tokenExpired_shouldNotSaveToken() throws ParseException, JOSEException {
        // given
        LogoutRequest request = new LogoutRequest();
        request.setToken("expired-token");

        doThrow(new AppException(ErrorCode.UNAUTHENTICATED))
                .when(authenticationService)
                .verifyToken("expired-token", true);

        // when
        authenticationService.logout(request);

        // then
        verify(invalidatedTokenRepository, never()).save(any());
    }

    @Test
    void forgotPassword_success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setLoginIdentifier("user1");

        Account account = new Account();
        account.setEmail("test@gmail.com");

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.of(account));

        when(otpTokenRepository.findByAccount(account)).thenReturn(Optional.empty());

        // when
        authenticationService.forgotPassword(request);

        // then
        verify(otpTokenRepository).save(any(OtpToken.class));
        verify(applicationEventPublisher).publishEvent(any(PasswordResetEvent.class));
    }

    @Test
    void forgotPassword_userNotExist_noException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setLoginIdentifier("unknown");

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authenticationService.forgotPassword(request));

        verifyNoInteractions(otpTokenRepository);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void resetPassword_accountNotFound_shouldThrowException() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginIdentifier("not-exist");

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.empty());

        // when & then
        AppException ex = assertThrows(AppException.class, () -> authenticationService.resetPassword(request));

        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }

    @Test
    void resetPassword_noOtp_shouldThrowUnauthenticated() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginIdentifier("user1");

        Account account = new Account();

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.of(account));

        when(otpTokenRepository.findByAccount(account)).thenReturn(Optional.empty());

        // when & then
        AppException ex = assertThrows(AppException.class, () -> authenticationService.resetPassword(request));

        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    @Test
    void resetPassword_otpExpired() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginIdentifier("user1");
        request.setOtpCode("123456");

        Account account = new Account();

        OtpToken otpToken = OtpToken.builder()
                .account(account)
                .code("123456")
                .expiryTime(Instant.now().minusSeconds(60))
                .build();

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.of(account));

        when(otpTokenRepository.findByAccount(account)).thenReturn(Optional.of(otpToken));

        AppException ex = assertThrows(AppException.class, () -> authenticationService.resetPassword(request));

        assertEquals(ErrorCode.OTP_EXPIRED, ex.getErrorCode());
    }

    @Test
    void resetPassword_invalidOtp_shouldThrow() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginIdentifier("user1");
        request.setOtpCode("000000");

        Account account = new Account();

        OtpToken otp = new OtpToken();
        otp.setCode("123456");
        otp.setExpiryTime(Instant.now().plusSeconds(300));

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.of(account));

        when(otpTokenRepository.findByAccount(account)).thenReturn(Optional.of(otp));

        // when & then
        AppException ex = assertThrows(AppException.class, () -> authenticationService.resetPassword(request));

        assertEquals(ErrorCode.INVALID_OTP, ex.getErrorCode());
        verify(otpTokenRepository, never()).delete(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void resetPassword_success() {
        // given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setLoginIdentifier("user1");
        request.setOtpCode("123456");
        request.setNewPassword("new-password");

        Account account = new Account();

        OtpToken otp = new OtpToken();
        otp.setCode("123456");
        otp.setExpiryTime(Instant.now().plusSeconds(300));

        when(accountRepository.findByUsernameOrEmailOrPhoneNumber(any(), any(), any()))
                .thenReturn(Optional.of(account));

        when(otpTokenRepository.findByAccount(account)).thenReturn(Optional.of(otp));

        // when
        authenticationService.resetPassword(request);

        // then
        verify(accountRepository).save(account);
        verify(otpTokenRepository).delete(otp);
        assertNotNull(account.getPassword());
    }

    @Test
    void generateOtpCode_shouldBe6Digits() {
        String otp = authenticationService.generateOtpCode();

        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void refreshToken_success() throws Exception {
        // GIVEN
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken("old-token");

        SignedJWT signedJWT = mock(SignedJWT.class);
        Date expiryTime = new Date(System.currentTimeMillis() + 60_000);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID("jti-123")
                .subject("user1")
                .expirationTime(expiryTime)
                .build();

        when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        // Mock verifyToken()
        doReturn(signedJWT)
                .when(authenticationService)
                .verifyToken("old-token", true);

        // Mock account
        Account account = new Account();
        account.setUsername("user1");

        when(accountRepository.findByUsername("user1"))
                .thenReturn(Optional.of(account));

        when(tokenService.generateToken(account))
                .thenReturn("new-token");

        // WHEN
        AuthenticationResponse response =
                authenticationService.refreshToken(request);

        // THEN
        assertNotNull(response);
        assertEquals("new-token", response.getToken());
        assertTrue(response.isAuthenticated());

        // Verify old token invalidated
        verify(invalidatedTokenRepository).save(
                argThat(token ->
                        token.getId().equals("jti-123") &&
                                token.getExpiryTime().equals(expiryTime)
                )
        );
    }

    @Test
    void refreshToken_userNotExist_throwException() throws Exception {
        // GIVEN
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken("old-token");

        SignedJWT signedJWT = mock(SignedJWT.class);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID("jti-123")
                .subject("unknown-user")
                .expirationTime(new Date())
                .build();

        when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        doReturn(signedJWT)
                .when(authenticationService)
                .verifyToken("old-token", true);

        when(accountRepository.findByUsername("unknown-user"))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        AppException exception = assertThrows(
                AppException.class,
                () -> authenticationService.refreshToken(request)
        );

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }
}
