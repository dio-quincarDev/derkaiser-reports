package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.dto.request.LoginRequest;
import com.derkaiser.auth.commons.dto.request.UserEntityRequest;
import com.derkaiser.auth.commons.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse createUser(UserEntityRequest userEntityRequest);
    TokenResponse login(LoginRequest loginRequest);


    String getCurrentUserEmail();
}
