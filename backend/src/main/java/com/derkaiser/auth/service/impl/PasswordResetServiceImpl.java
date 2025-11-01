package com.derkaiser.auth.service.impl;

import com.derkaiser.auth.commons.model.entity.PasswordResetToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.repository.PasswordResetTokenRepository;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.PasswordResetService;
import com.derkaiser.auth.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserEntityRepository userEntityRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional
    public void passwordResetRequest(String email) {
        log.info("Solicitud de reset de password para email: {}", email);

        // Buscar usuario
        Optional<UserEntity> userOptional = userEntityRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.info("Email no encontrado, pero retornando success por seguridad: {}", email);
            return; // Retornar sin error
        }

        UserEntity user = userOptional.get();
        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken resetToken = PasswordResetToken.create(user);
        passwordResetTokenRepository.save(resetToken);


        log.info("Token de reset creado para usuario: {}", user.getEmail());

        // Construir URL de reset
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken.getToken();

        try {
           sendPasswordEmailReset(user.getEmail(), user.getFirstName(), resetUrl);
            log.info("Email de reset enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de reset a {}: {}", user.getEmail(), e.getMessage());
            // No lanzar excepción para no revelar si el email existe
        }

    }

    @Override
    @Transactional
    public void passwordReset(String token, String newPassword) {

        log.info("Intentando resetear password con token: {}", token);

        // Buscar token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Token de reset no encontrado: {}", token);
                    return new IllegalArgumentException("Token de reset inválido");
                });

        // Validar que no haya sido usado
        if (resetToken.getUsed()) {
            log.warn("Intento de usar token de reset ya utilizado: {}", token);
            throw new IllegalArgumentException("Este token ya ha sido utilizado");
        }

        // Validar expiración
        if (resetToken.isExpired()) {
            log.warn("Intento de usar token de reset expirado: {}", token);
            throw new IllegalArgumentException("El token ha expirado. Solicita un nuevo enlace de reset");
        }

        // Obtener usuario
        UserEntity user = resetToken.getUserEntity();

        // Cambiar password
        user.setPassword(passwordEncoder.encode(newPassword));
        userEntityRepository.save(user);


        // Marcar token como usado
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // CRÍTICO: Invalidar todos los refresh tokens (cerrar todas las sesiones)
        refreshTokenService.deleteAllUserTokens(user.getEmail());

        log.info("Password reseteado exitosamente para usuario: {} y sesiones cerradas", user.getEmail());


    }


    @Override
    @Transactional
    public boolean tokenResetVerification(String token) {

        log.info("Validando token de reset: {}", token);

        Optional<PasswordResetToken> resetTokenOptional = passwordResetTokenRepository.findByToken(token);

        if (resetTokenOptional.isEmpty()) {
            log.info("Token no encontrado: {}", token);
            return false;
        }

        PasswordResetToken resetToken = resetTokenOptional.get();

        boolean valid = !resetToken.getUsed() && !resetToken.isExpired();

        log.info("Token {} es válido: {}", token, valid);
        return valid;
    }

    private void sendPasswordEmailReset(String toEmail, String firstName, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Recuperar contraseña - Infoplazas AIP");
        message.setText(generateMessagePasswordReset(firstName, resetUrl));

        mailSender.send(message);
    }

    private String generateMessagePasswordReset(String firstName, String resetUrl) {
        return String.format("""
                Hola %s,
                
                Hemos recibido una solicitud para resetear tu contraseña.
                
                Haz clic en el siguiente enlace para crear una nueva contraseña:
                
                %s
                
                Este enlace expirará en 1 hora.
                
                Si no solicitaste este cambio, puedes ignorar este mensaje de forma segura.
                
                                Saludos,
                                Equipo de Infoplazas AIP
                                """, firstName, resetUrl);
                                }
                }
