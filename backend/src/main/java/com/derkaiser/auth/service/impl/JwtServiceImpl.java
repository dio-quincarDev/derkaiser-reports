package com.derkaiser.auth.service.impl;

import com.derkaiser.auth.commons.model.entity.BlacklistedToken;
import com.derkaiser.auth.repository.BlacklistedTokenRepository;
import com.derkaiser.auth.service.JwtService;
import com.derkaiser.exceptions.auth.InvalidJwtTokenException;
import com.derkaiser.exceptions.auth.MissingJwtClaimException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtServiceImpl implements JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);

    private final SecretKey secretKey;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Value("${jwt.access-token.expiration:900000}") // 15 minutos en milisegundos
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:604800000}") // 7 días en milisegundos
    private long refreshTokenExpiration;

    public JwtServiceImpl(@Value("${jwt.secret:${JWT_SECRET}}") String secret,
                          BlacklistedTokenRepository blacklistedTokenRepository) {
        if (secret.getBytes().length < 32) {
            throw new IllegalArgumentException("La clave secreta de JWT debe tener al menos 32 caracteres.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }



    // ⚠️ NUEVO MÉTODO: Access Token (15 min)
    @Override
    public String generateAccessToken(String email, String role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenExpiration);

        String normalizedRole = role.startsWith("ROLE_") ? role.substring(5) : role;

        return Jwts.builder()
                .subject(email)
                .claim("role", normalizedRole)
                .claim("type", "access") // ⚠️ Identificador de tipo
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    // ⚠️ NUEVO MÉTODO: Refresh Token (7 días)
    @Override
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("type", "refresh") // ⚠️ Identificador de tipo
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    // ⚠️ NUEVO MÉTODO: Validar token
    @Override
    public boolean validateToken(String token) {
        try {
            // Verificar si el token está expirado
            if (isExpired(token)) {
                return false;
            }

            // Verificar si el token está en la lista negra
            if (isTokenBlacklisted(token)) {
                log.warn("Token encontrado en lista negra: {}", token);
                return false;
            }

            getClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error al parsear JWT: {}, Causa: {}", e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "N/A");
            throw new InvalidJwtTokenException("Token JWT inválido o expirado", e);
        }
    }

    @Override
    public boolean isExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public String extractRole(String token) {
        return Optional.ofNullable(getClaims(token).get("role", String.class))
                .orElseThrow(() -> new MissingJwtClaimException("El claim 'role' es requerido en el token"));
    }

    @Override
    public String extractEmail(String token) {
        return Optional.ofNullable(getClaims(token).getSubject())
                .orElseThrow(() -> new MissingJwtClaimException("El claim 'subject' (email) es requerido en el token"));
    }

    @Override
    public void blacklistToken(String token, String tokenType) {
        log.info("Añadiendo token a la lista negra: {} (tipo: {})", token, tokenType);

        // Extraer la fecha de expiración del token para usarla en la lista negra
        try {
            Claims claims = getClaims(token);
            Date expirationDate = claims.getExpiration();

            BlacklistedToken.TokenType type = "ACCESS".equals(tokenType.toUpperCase()) ?
                    BlacklistedToken.TokenType.ACCESS : BlacklistedToken.TokenType.REFRESH;

            BlacklistedToken blacklistedToken = BlacklistedToken.create(token, type,
                    expirationDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());

            blacklistedTokenRepository.save(blacklistedToken);
            log.info("Token añadido exitosamente a la lista negra: {}", token);
        } catch (Exception e) {
            log.error("Error al añadir token a la lista negra: {}", e.getMessage());
            throw new RuntimeException("Error al intentar invalidar token", e);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }
}