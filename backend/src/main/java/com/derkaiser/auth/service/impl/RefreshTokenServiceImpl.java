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
import com.derkaiser.exceptions.auth.UserInactiveException;
import com.derkaiser.exceptions.auth.UserNotVerifiedException;
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

        // Validar token JWT primero
        if (!jwtService.validateToken(refreshToken)) {
            log.warn("Refresh token JWT inválido o expirado");
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }

        RefreshToken existingToken = null;
        UserEntity userEntity = null;

        try {
            // Primero intentamos con JOIN FETCH
            existingToken = refreshTokenRepository.findByTokenWithUser(refreshToken).orElse(null);

            if (existingToken != null && existingToken.getUserEntity() != null) {
                userEntity = existingToken.getUserEntity();
            } else {
                // Si la carga con JOIN FETCH falla, intentamos cargar el refresh token y luego forzamos la carga del usuario
                existingToken = refreshTokenRepository.findByToken(refreshToken).orElse(null);
                if (existingToken != null) {
                    // Forzamos la carga del userEntity en la sesión actual
                    userEntity = existingToken.getUserEntity();
                    if (userEntity != null) {
                        // Aseguramos que los datos del usuario estén disponibles
                        userEntity.getId();
                        userEntity.getEmail();
                        userEntity.getRole();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error al recuperar refresh token con usuario: {}", e.getMessage());
            throw new IllegalArgumentException("Error al procesar el refresh token", e);
        }

        // Validar que ambas entidades estén disponibles
        if (existingToken == null) {
            log.warn("Refresh token no encontrado en base de datos");
            throw new IllegalArgumentException("Refresh token no encontrado");
        }

        if (userEntity == null) {
            log.warn("Refresh token encontrado pero el usuario asociado es nulo para token: {}", refreshToken);
            throw new IllegalArgumentException("Refresh token inválido - Usuario asociado es nulo");
        }

        // Validar campos esenciales del usuario
        if (userEntity.getId() == null || userEntity.getEmail() == null) {
            log.warn("Usuario asociado incompleto - ID o email nulo para token: {}", refreshToken);
            throw new IllegalArgumentException("Refresh token inválido - Usuario asociado incompleto");
        }

        // Verificar expiración en la base de datos
        if (existingToken.isExpired()) {
            String userEmail = userEntity.getEmail(); // Acceso seguro ya validado
            log.warn("Refresh token expirado para usuario: {}", userEmail);
            refreshTokenRepository.delete(existingToken);

            // SOLUCIÓN: Solo blacklist si el token JWT no está expirado
            if (!jwtService.isExpired(refreshToken)) {
                jwtService.blacklistToken(refreshToken, "REFRESH");
            }

            throw new IllegalArgumentException("Refresh token expirado. Por favor, inicia sesión nuevamente");
        }

        // Validar estado del usuario
        if (!userEntity.getActive()) {
            log.warn("Usuario inactivo intenta usar refresh token: {}", userEntity.getEmail());
            refreshTokenRepository.delete(existingToken);
            jwtService.blacklistToken(refreshToken, "REFRESH");
            throw new UserInactiveException("Tu cuenta no está activa");
        }

        if (!userEntity.getVerificatedEmail()) {
            log.warn("Usuario no verificado intenta usar refresh token: {}", userEntity.getEmail());
            refreshTokenRepository.delete(existingToken);
            jwtService.blacklistToken(refreshToken, "REFRESH");
            throw new UserNotVerifiedException("Tu cuenta no está verificada. Por favor revisa tu email para verificarla.");
        }

        // Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(userEntity.getEmail(), userEntity.getRole().name());

        // Rotación: invalidar el token actual y crear uno nuevo
        jwtService.blacklistToken(refreshToken, "REFRESH");
        refreshTokenRepository.delete(existingToken);

        // Crear nuevo refresh token
        String newRefreshToken = createRefreshToken(userEntity);

        log.info("Token rotado exitosamente para usuario: {}", userEntity.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(900_000L)
                .build();
    }

    @Override
    @Transactional
    public void deleteByToken(String refreshToken) {
        log.info("Eliminando refresh token específico");

        refreshTokenRepository.findByTokenWithUser(refreshToken)
                .ifPresent(token -> {
                    // Primero añadir a la lista negra antes de eliminar de la base de datos
                    jwtService.blacklistToken(refreshToken, "REFRESH");
                    refreshTokenRepository.delete(token);
                    String userEmail = token.getUserEntity() != null ? token.getUserEntity().getEmail() : "unknown";
                    log.info("Refresh token eliminado y añadido a lista negra para usuario: {}", userEmail);
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
