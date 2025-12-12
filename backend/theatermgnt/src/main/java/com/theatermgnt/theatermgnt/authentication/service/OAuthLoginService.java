package com.theatermgnt.theatermgnt.authentication.service;

import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;

public interface OAuthLoginService {
    AuthenticationResponse loginWithGoogleCode(String code);

    String generateTemporaryPassword();
}
