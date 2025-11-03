package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.entity.VerificationToken;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.repository.VerificationTokenRepository;
import com.derkaiser.auth.service.impl.EmailVerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    private UserEntity user;
    private VerificationToken verificationToken;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .active(false)
                .verificatedEmail(false)
                .build();

        verificationToken = VerificationToken.builder()
                .id(UUID.randomUUID())
                .token("test-token")
                .userEntity(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        // Inject @Value fields
        ReflectionTestUtils.setField(emailVerificationService, "frontendUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(emailVerificationService, "fromEmail", "no-reply@infoplazas.com");
    }

    @Test
    void createAndSendVerificationEmail_shouldCreateTokenAndSendEmailSuccessfully() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailVerificationService.createAndSendVerificationEmail(user);

        verify(verificationTokenRepository, times(1)).deleteByUserEntity(user);

        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository, times(1)).save(tokenCaptor.capture());
        VerificationToken savedToken = tokenCaptor.getValue();

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("no-reply@infoplazas.com", sentMessage.getFrom());
        assertEquals("test@test.com", sentMessage.getTo()[0]);
        assertTrue(sentMessage.getSubject().contains("Verifica tu cuenta"));
        assertTrue(sentMessage.getText().contains("http://localhost:8080/verify?token=" + savedToken.getToken()));
    }

    @Test
    void createAndSendVerificationEmail_shouldThrowRuntimeExceptionWhenEmailSendingFails() {
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);
        doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailVerificationService.createAndSendVerificationEmail(user));

        assertTrue(exception.getMessage().contains("Error al enviar email de verificación"));
        verify(verificationTokenRepository, times(1)).deleteByUserEntity(user);
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void emailVerification_shouldVerifyEmailSuccessfully() {
        when(verificationTokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));
        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(user);
        doNothing().when(verificationTokenRepository).delete(any(VerificationToken.class));

        emailVerificationService.emailVerification("test-token");

        assertTrue(user.getActive());
        assertTrue(user.getVerificatedEmail());
        verify(verificationTokenRepository, times(1)).findByToken("test-token");
        verify(userEntityRepository, times(1)).save(user);
        verify(verificationTokenRepository, times(1)).delete(verificationToken);
    }

    @Test
    void emailVerification_shouldThrowExceptionWhenTokenNotFound() {
        when(verificationTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailVerificationService.emailVerification("non-existent-token"));

        assertEquals("Token de verificación inválido", exception.getMessage());
        verify(verificationTokenRepository, times(1)).findByToken("non-existent-token");
        verify(userEntityRepository, never()).save(any(UserEntity.class));
        verify(verificationTokenRepository, never()).delete(any(VerificationToken.class));
    }

    @Test
    void emailVerification_shouldThrowExceptionWhenTokenIsExpired() {
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Set token as expired
        when(verificationTokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailVerificationService.emailVerification("expired-token"));

        assertEquals("El token ha expirado. Solicita un nuevo email de verificación", exception.getMessage());
        verify(verificationTokenRepository, times(1)).findByToken("expired-token");
        verify(userEntityRepository, never()).save(any(UserEntity.class));
        verify(verificationTokenRepository, never()).delete(any(VerificationToken.class));
    }

    @Test
    void resendVerificationEmail_shouldResendEmailSuccessfully() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailVerificationService.resendVerificationEmail("test@test.com");

        verify(userEntityRepository, times(1)).findByEmail("test@test.com");
        verify(verificationTokenRepository, times(1)).deleteByUserEntity(user); // Called by createAndSendVerificationEmail
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class)); // Called by createAndSendVerificationEmail
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class)); // Called by createAndSendVerificationEmail
    }

    @Test
    void resendVerificationEmail_shouldThrowExceptionWhenUserNotFound() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailVerificationService.resendVerificationEmail("non-existent@test.com"));

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userEntityRepository, times(1)).findByEmail("non-existent@test.com");
        verify(verificationTokenRepository, never()).deleteByUserEntity(any(UserEntity.class));
        verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void resendVerificationEmail_shouldThrowExceptionWhenEmailAlreadyVerified() {
        user.setVerificatedEmail(true);
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> emailVerificationService.resendVerificationEmail("test@test.com"));

        assertEquals("Este email ya está verificado", exception.getMessage());
        verify(userEntityRepository, times(1)).findByEmail("test@test.com");
        verify(verificationTokenRepository, never()).deleteByUserEntity(any(UserEntity.class));
        verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}