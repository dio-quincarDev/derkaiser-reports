package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.model.entity.PasswordResetToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.repository.PasswordResetTokenRepository;
import com.derkaiser.auth.repository.RefreshTokenRepository;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.impl.PasswordResetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private UserEntity user;
    private PasswordResetToken passwordResetToken;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .firstName("Test")
                .password("oldPassword")
                .build();

        passwordResetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .token("reset-token")
                .userEntity(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(passwordResetService, "fromEmail", "no-reply@infoplazas.com");
    }

    @Test
    void passwordResetRequest_shouldSendEmailWhenUserExists() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(passwordResetToken);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        passwordResetService.passwordResetRequest("test@test.com");

        verify(userEntityRepository, times(1)).findByEmail("test@test.com");
        verify(passwordResetTokenRepository, times(1)).deleteByUserEntity(user);
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void passwordResetRequest_shouldReturnSilentlyWhenUserNotFound() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        passwordResetService.passwordResetRequest("nonexistent@test.com");

        verify(userEntityRepository, times(1)).findByEmail("nonexistent@test.com");
        verify(passwordResetTokenRepository, never()).deleteByUserEntity(any());
        verify(passwordResetTokenRepository, never()).save(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void passwordResetRequest_shouldNotThrowExceptionWhenEmailSendingFails() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(passwordResetToken);
        doThrow(new MailSendException("Failed to send")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> passwordResetService.passwordResetRequest("test@test.com"));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void passwordReset_shouldResetPasswordSuccessfully() {
        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        passwordResetService.passwordReset("reset-token", "newPassword");

        verify(passwordResetTokenRepository, times(1)).findByToken("reset-token");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userEntityRepository, times(1)).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
        verify(passwordResetTokenRepository, times(1)).delete(passwordResetToken);
        verify(refreshTokenRepository, times(1)).deleteByUserEntity(user);
    }

    @Test
    void passwordReset_shouldThrowExceptionWhenTokenNotFound() {
        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.passwordReset("invalid-token", "newPassword"));

        assertEquals("Token de reset inválido", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userEntityRepository, never()).save(any());
    }

    @Test
    void passwordReset_shouldThrowExceptionWhenTokenIsExpired() {
        passwordResetToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(passwordResetToken));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.passwordReset("expired-token", "newPassword"));

        assertEquals("El token ha expirado. Solicita un nuevo enlace de reset", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userEntityRepository, never()).save(any());
    }
}