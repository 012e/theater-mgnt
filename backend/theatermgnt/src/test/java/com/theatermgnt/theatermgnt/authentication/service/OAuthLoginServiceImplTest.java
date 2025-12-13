package com.theatermgnt.theatermgnt.authentication.service;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.service.RegistrationService;
import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.dto.response.ExchangeTokenResponse;
import com.theatermgnt.theatermgnt.authentication.dto.response.OutBoundUserResponse;
import com.theatermgnt.theatermgnt.authentication.repository.httpClient.OutboundIdentityClient;
import com.theatermgnt.theatermgnt.authentication.repository.httpClient.OutboundUserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OAuthLoginServiceImplTest {
    @InjectMocks
    OAuthLoginServiceImpl oAuthLoginService;

    @Mock
    OutboundIdentityClient outboundIdentityClient;

    @Mock
    OutboundUserClient outboundUserClient;

    @Mock
    RegistrationService registrationService;

    @Mock
    TokenService tokenService;

    @BeforeEach
    void setup() {
        oAuthLoginService.CLIENT_ID = "client-id";
        oAuthLoginService.CLIENT_SECRET = "client-secret";
        oAuthLoginService.REDIRECT_URI = "http://localhost/callback";
    }

    @Test
    void loginWithGoogleCode_success() {
        // given
        String code = "google-auth-code";

        // 1. Google exchange token response
        ExchangeTokenResponse tokenResponse = ExchangeTokenResponse.builder()
                .accessToken("google-access-token")
                .build();

        when(outboundIdentityClient.exchangeToken(any()))
                .thenReturn(tokenResponse);

        // 2. Google user inf
        OutBoundUserResponse userInfo = OutBoundUserResponse.builder()
                .email("test@gmail.com")
                .givenName("Test")
                .familyName("User")
                .build();

        when(outboundUserClient.getUserInfo("json", "google-access-token"))
                .thenReturn(userInfo);

        // 3. Account
        Account account = new Account();
        account.setId("acc-1");

        when(registrationService.registerOAuthCustomer(any()))
                .thenReturn(account);

        // 4. JWT token
        when(tokenService.generateToken(account))
                .thenReturn("jwt-token");

        // when
        AuthenticationResponse response =
                oAuthLoginService.loginWithGoogleCode(code);

        // then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        verify(outboundIdentityClient).exchangeToken(any());
        verify(outboundUserClient)
                .getUserInfo("json", "google-access-token");
        verify(registrationService).registerOAuthCustomer(any());
        verify(tokenService).generateToken(account);
    }

    @Test
    void loginWithGoogleCode_exchangeTokenFail_throwException() {
        when(outboundIdentityClient.exchangeToken(any()))
                .thenThrow(new RuntimeException("Google error"));

        assertThrows(
                RuntimeException.class,
                () -> oAuthLoginService.loginWithGoogleCode("code")
        );
    }


}


