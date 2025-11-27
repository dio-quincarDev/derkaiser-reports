package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.dto.response.TokenResponse;
import io.jsonwebtoken.Claims;

public interface JwtService {


    // ⚠️ NUEVO MÉTODO: Validar token
    boolean validateToken(String token);

    Claims getClaims(String token);
    boolean isExpired(String token);
    String extractRole(String token);
    String extractEmail(String token);
    String generateAccessToken(String email, String role);
    String generateRefreshToken(String email);

    // Métodos para lista negra de tokens
    void blacklistToken(String token, String tokenType);
    boolean isTokenBlacklisted(String token);
}
