package com.derkaiser.auth.service.impl;

import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.model.entity.RefreshToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;
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
        log.info("Validando y refrescando access token");

        if (!jwtService.validateToken(refreshToken)) {
            log.warn("Refresh token JWT inválido o expirado");
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }

        RefreshToken toEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()-> {
                    log.warn("Refresh token no encontrado en base de datos");
                    return new IllegalArgumentException("Refresh token no encontrado");
                });
        if (toEntity.isExpired()) {
            log.warn("Refresh token expirado para usuario: {}", toEntity.getUserEntity().getEmail());
            refreshTokenRepository.delete(toEntity);
            throw new IllegalArgumentException("Refresh token expirado. Por favor, inicia sesión nuevamente");
        }

        UserEntity user = toEntity.getUserEntity();

        if (!user.getActive() || !user.getVerificatedEmail()) {
            log.warn("Usuario inactivo o no verificado intenta usar refresh token: {}", user.getEmail());
            refreshTokenRepository.delete(toEntity);
            throw new IllegalArgumentException("Tu cuenta no está activa o verificada");
        }

        // Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());

        log.info("Nuevo access token generado para usuario: {}", user.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Mismo refresh token
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
                    refreshTokenRepository.delete(token);
                    log.info("Refresh token eliminado para usuario: {}", token.getUserEntity().getEmail());
                });

    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTokens() {
        log.info("Iniciando limpieza de refresh tokens expirados");

        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());

        log.info("Limpieza de tokens expirados completada");
    }

}
