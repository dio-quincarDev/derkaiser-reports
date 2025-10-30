package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.dto.response.TokenResponse;
import io.jsonwebtoken.Claims;

public interface JwtService {
    TokenResponse generateToken(String token);
    Claims getClaims(String token);
    boolean isExpired(String token);
    String extractRole(String token);
    String extractEmail(String token);

}
