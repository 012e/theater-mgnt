package com.theatermgnt.theatermgnt.authentication.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.theatermgnt.theatermgnt.authentication.dto.request.*;
import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException;

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void logout(LogoutRequest request) throws ParseException, JOSEException;

    AuthenticationResponse refreshToken(RefreshTokenRequest refreshToken) throws ParseException, JOSEException;

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    SignedJWT verifyToken(String token, boolean isRefreshToken) throws JOSEException, ParseException;

    String generateOtpCode();
}
