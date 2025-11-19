package com.derkaiser.auth.controller;

import com.derkaiser.auth.commons.dto.request.*;
import com.derkaiser.auth.commons.dto.response.MessageResponse;
import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.dto.response.UserResponse;
import com.derkaiser.constants.ApiPathConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.AUTH_ROUTE)
@Tag(name = "Autenticación", description = "Operaciones de autenticación y gestión de usuarios")
public interface AuthApi {

    @Operation(
        summary = "Registrar un nuevo usuario",
        description = "Crea un nuevo usuario en el sistema. Se enviará un email de verificación a la dirección proporcionada."
    )
    @ApiResponse(
        responseCode = "201",
        description = "Usuario creado exitosamente",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = TokenResponse.class)
        )
    )
    @PostMapping("/register")
    ResponseEntity<TokenResponse> createUser(@RequestBody @Valid UserEntityRequest userEntityRequest);

    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica al usuario con email y contraseña, retornando tokens JWT."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Inicio de sesión exitoso",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = TokenResponse.class)
        )
    )
    @PostMapping("/login")
    ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest);

    @Operation(
        summary = "Obtener información del usuario autenticado",
        description = "Retorna la información del usuario actualmente autenticado."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Información de usuario obtenida exitosamente",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserResponse.class)
        )
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<UserResponse> getUser();

    @Operation(
        summary = "Verificar email",
        description = "Verifica la dirección de email del usuario usando un token enviado por correo."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Email verificado exitosamente"
    )
    @GetMapping("/verify")
    ResponseEntity<MessageResponse> verifyEmail(@Parameter(description = "Token de verificación enviado por email", required = true) @RequestParam String token);

    @Operation(
        summary = "Reenviar email de verificación",
        description = "Reenvía el email de verificación a la dirección especificada."
    )
    @PostMapping("/resend-verification")
    ResponseEntity<MessageResponse> resendVerification(@RequestBody @Valid ForgotPasswordRequest request);

    @Operation(
        summary = "Solicitar restablecimiento de contraseña",
        description = "Envía un email con instrucciones para restablecer la contraseña."
    )
    @PostMapping("/forgot-password")
    ResponseEntity<MessageResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request);

    @Operation(
        summary = "Restablecer contraseña",
        description = "Restablece la contraseña del usuario usando un token de restablecimiento."
    )
    @PostMapping("/reset-password")
    ResponseEntity<MessageResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request);

    @Operation(
        summary = "Refrescar token de acceso",
        description = "Obtiene un nuevo token de acceso usando un token de refresco."
    )
    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request);

    @Operation(
        summary = "Cerrar sesión",
        description = "Invalida el token de refresco, cerrando la sesión del usuario."
    )
    @PostMapping("/logout")
    ResponseEntity<MessageResponse> logout(@RequestBody @Valid RefreshTokenRequest request);
}
