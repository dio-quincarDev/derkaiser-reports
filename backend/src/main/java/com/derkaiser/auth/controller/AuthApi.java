package com.derkaiser.auth.controller;

import com.derkaiser.auth.commons.dto.request.*;
import com.derkaiser.auth.commons.dto.response.MessageResponse;
import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.dto.response.UserResponse;
import com.derkaiser.constants.ApiPathConstants;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE)
public interface AuthApi {
    @PostMapping("/register")
    ResponseEntity<TokenResponse> createUser(@RequestBody @Valid UserEntityRequest userEntityRequest);

    @PostMapping("/login")
    ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest);

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<UserResponse> getUser();

    @GetMapping("/verify")
    ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token);

    @PostMapping("/resend-verification")
    ResponseEntity<MessageResponse> resendVerification(@RequestBody @Valid ForgotPasswordRequest request);

    @PostMapping("/forgot-password")
    ResponseEntity<MessageResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request);

    @PostMapping("/reset-password")
    ResponseEntity<MessageResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request);

    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request);

    @PostMapping("/logout")
    ResponseEntity<MessageResponse> logout(@RequestBody @Valid RefreshTokenRequest request);
}
