package com.derkaiser.auth.service.impl;

import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.model.entity.BlacklistedToken;
import com.derkaiser.auth.commons.model.entity.RefreshToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.repository.BlacklistedTokenRepository;
import com.derkaiser.auth.repository.RefreshTokenRepository;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.JwtService;
import com.derkaiser.auth.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserEntityRepository userEntityRepository;
    private final JwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    @Transactional
    public String createRefreshToken(UserEntity userEntity) {
        log.info("Creando refresh token para usuario: {}", userEntity.getEmail());

        String tokenValue = jwtService.generateRefreshToken(userEntity.getEmail());

        RefreshToken refreshToken = RefreshToken.create(userEntity, tokenValue);
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token creado exitosamente para usuario: {}", userEntity.getEmail());

        return tokenValue;
    }

    @Override
    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        log.info("Validando y refrescando access token con rotación");

        // Validar token JWT
        if (!jwtService.validateToken(refreshToken)) {
            log.warn("Refresh token JWT inválido o expirado");
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }

        RefreshToken existingToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()-> {
                    log.warn("Refresh token no encontrado en base de datos");
                    return new IllegalArgumentException("Refresh token no encontrado");
                });
        if (existingToken.isExpired()) {
            log.warn("Refresh token expirado para usuario: {}", existingToken.getUserEntity().getEmail());
            refreshTokenRepository.delete(existingToken);
            jwtService.blacklistToken(refreshToken, "REFRESH"); // Añadir a la lista negra
            throw new IllegalArgumentException("Refresh token expirado. Por favor, inicia sesión nuevamente");
        }

        UserEntity user = existingToken.getUserEntity();

        if (!user.getActive() || !user.getVerificatedEmail()) {
            log.warn("Usuario inactivo o no verificado intenta usar refresh token: {}", user.getEmail());
            refreshTokenRepository.delete(existingToken);
            jwtService.blacklistToken(refreshToken, "REFRESH"); // Añadir a la lista negra
            throw new IllegalArgumentException("Tu cuenta no está activa o verificada");
        }

        // Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());

        // Rotación: invalidar el token actual y crear uno nuevo
        jwtService.blacklistToken(refreshToken, "REFRESH"); // Añadir el token actual a la lista negra
        refreshTokenRepository.delete(existingToken); // Eliminar de la base de datos

        // Crear nuevo refresh token
        String newRefreshToken = createRefreshToken(user); // Usar el método existente

        log.info("Token rotado exitosamente para usuario: {}", user.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken) // Nuevo token en lugar del anterior
                .tokenType("Bearer")
                .expiresIn(900_000L) // 15 minutos
                .build();
    }

    @Override
    @Transactional
    public void deleteByToken(String refreshToken) {
        log.info("Eliminando refresh token específico");

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    // Primero añadir a la lista negra antes de eliminar de la base de datos
                    jwtService.blacklistToken(refreshToken, "REFRESH");
                    refreshTokenRepository.delete(token);
                    log.info("Refresh token eliminado y añadido a lista negra para usuario: {}", token.getUserEntity().getEmail());
                });

    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTokens() {
        log.info("Iniciando limpieza de refresh tokens y tokens en lista negra expirados");

        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        blacklistedTokenRepository.deleteExpiredTokens(LocalDateTime.now());

        log.info("Limpieza de tokens expirados completada");
    }

}
