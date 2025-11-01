package com.derkaiser.auth.controller.impl;

import com.derkaiser.auth.commons.dto.request.ForgotPasswordRequest;
import com.derkaiser.auth.commons.dto.request.LoginRequest;
import com.derkaiser.auth.commons.dto.request.RefreshTokenRequest;
import com.derkaiser.auth.commons.dto.request.ResetPasswordRequest;
import com.derkaiser.auth.commons.dto.request.UserEntityRequest;
import com.derkaiser.auth.commons.dto.response.MessageResponse;
import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.dto.response.UserResponse;
import com.derkaiser.auth.controller.AuthApi;
import com.derkaiser.auth.service.AuthService;
import com.derkaiser.auth.service.EmailVerificationService;
import com.derkaiser.auth.service.PasswordResetService;
import com.derkaiser.auth.service.RefreshTokenService;
import com.derkaiser.auth.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final UserManagementService userManagementService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;


    @Override
    public ResponseEntity<TokenResponse> createUser(@Valid UserEntityRequest userEntityRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(userEntityRequest));
    }

    @Override
    public ResponseEntity<TokenResponse> login(@Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<UserResponse> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Email es el username
        UserResponse userResponse = userManagementService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        emailVerificationService.emailVerification(token);
        return ResponseEntity.ok(MessageResponse.of("Email verificado exitosamente."));
    }

    @Override
    public ResponseEntity<MessageResponse> resendVerification(@Valid ForgotPasswordRequest request) {
        emailVerificationService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of("Email de verificación reenviado. Revisa tu bandeja de entrada."));
    }

    @Override
    public ResponseEntity<MessageResponse> forgotPassword(@Valid ForgotPasswordRequest request) {
        passwordResetService.passwordResetRequest(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of("Si tu email está registrado, recibirás un enlace para resetear tu contraseña."));
    }

    @Override
    public ResponseEntity<MessageResponse> resetPassword(@Valid ResetPasswordRequest request) {
        passwordResetService.passwordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.of("Contraseña actualizada exitosamente."));
    }

    @Override
    public ResponseEntity<TokenResponse> refreshToken(@Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request.getRefreshToken()));
    }

    @Override
    public ResponseEntity<MessageResponse> logout(@Valid RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
        return ResponseEntity.ok(MessageResponse.of("Sesión cerrada exitosamente."));
    }
}
