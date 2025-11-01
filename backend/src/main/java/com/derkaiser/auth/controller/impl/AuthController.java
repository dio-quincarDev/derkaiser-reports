package com.derkaiser.auth.controller.impl;

import com.derkaiser.auth.commons.dto.request.LoginRequest;
import com.derkaiser.auth.commons.dto.request.UserEntityRequest;
import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.controller.AuthApi;
import com.derkaiser.auth.service.AuthService;
import com.derkaiser.auth.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final UserManagementService userManagementService;


    @Override
    public ResponseEntity<TokenResponse> createUser(UserEntityRequest userEntityRequest) {
        return null;
    }

    @Override
    public ResponseEntity<TokenResponse> login(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public ResponseEntity<String> getUser(@RequestAttribute(name = "X-User-Id") @Valid String userEntityId) {
        return ResponseEntity.ok(userEntityId);
    }
}
