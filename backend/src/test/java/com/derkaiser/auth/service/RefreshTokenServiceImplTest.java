package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.model.entity.RefreshToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.enums.UserRole;
import com.derkaiser.auth.repository.RefreshTokenRepository;
import com.derkaiser.auth.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private UserEntity user;
    private RefreshToken refreshToken;
    private final String refreshTokenString = "test-refresh-token";

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .role(UserRole.USER)
                .active(true)
                .verificatedEmail(true)
                .build();

        refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(refreshTokenString)
                .userEntity(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    void createRefreshToken_shouldCreateAndReturnToken() {
        when(jwtService.generateRefreshToken(anyString())).thenReturn(refreshTokenString);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        String tokenValue = refreshTokenService.createRefreshToken(user);

        assertEquals(refreshTokenString, tokenValue);
        verify(jwtService, times(1)).generateRefreshToken(user.getEmail());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void refreshAccessToken_shouldReturnNewAccessToken() {
        String newAccessToken = "new-access-token";
        when(jwtService.validateToken(refreshTokenString)).thenReturn(true);
        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken(user.getEmail(), user.getRole().name())).thenReturn(newAccessToken);

        TokenResponse response = refreshTokenService.refreshAccessToken(refreshTokenString);

        assertNotNull(response);
        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(refreshTokenString, response.getRefreshToken());
        verify(jwtService, times(1)).generateAccessToken(user.getEmail(), user.getRole().name());
    }

    @Test
    void refreshAccessToken_shouldThrowExceptionForInvalidJwt() {
        when(jwtService.validateToken(refreshTokenString)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> refreshTokenService.refreshAccessToken(refreshTokenString));

        assertEquals("Refresh token inválido o expirado", exception.getMessage());
        verify(refreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    void refreshAccessToken_shouldThrowExceptionWhenTokenNotFoundInDb() {
        when(jwtService.validateToken(refreshTokenString)).thenReturn(true);
        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> refreshTokenService.refreshAccessToken(refreshTokenString));

        assertEquals("Refresh token no encontrado", exception.getMessage());
    }

    @Test
    void refreshAccessToken_shouldThrowExceptionAndDeletesWhenTokenIsExpiredInDb() {
        refreshToken.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(jwtService.validateToken(refreshTokenString)).thenReturn(true);
        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> refreshTokenService.refreshAccessToken(refreshTokenString));

        assertEquals("Refresh token expirado. Por favor, inicia sesión nuevamente", exception.getMessage());
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void refreshAccessToken_shouldThrowExceptionAndDeletesWhenUserIsInactive() {
        user.setActive(false);
        when(jwtService.validateToken(refreshTokenString)).thenReturn(true);
        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> refreshTokenService.refreshAccessToken(refreshTokenString));

        assertEquals("Tu cuenta no está activa o verificada", exception.getMessage());
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void deleteByToken_shouldDeleteTokenWhenFound() {
        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        doNothing().when(refreshTokenRepository).delete(refreshToken);

        refreshTokenService.deleteByToken(refreshTokenString);

        verify(refreshTokenRepository, times(1)).findByToken(refreshTokenString);
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void deleteByToken_shouldDoNothingWhenTokenNotFound() {
        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.empty());

        refreshTokenService.deleteByToken(refreshTokenString);

        verify(refreshTokenRepository, times(1)).findByToken(refreshTokenString);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void cleanExpiredTokens_shouldCallRepositoryMethod() {
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        refreshTokenService.cleanExpiredTokens();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(refreshTokenRepository, times(1)).deleteExpiredTokens(captor.capture());
        assertNotNull(captor.getValue());
    }
}