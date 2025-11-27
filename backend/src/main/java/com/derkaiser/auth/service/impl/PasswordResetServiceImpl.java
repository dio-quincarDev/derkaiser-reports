package com.derkaiser.auth.service.impl;

import com.derkaiser.auth.commons.model.entity.PasswordResetToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.repository.PasswordResetTokenRepository;
import com.derkaiser.auth.repository.RefreshTokenRepository;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.PasswordResetService;
import com.derkaiser.auth.service.RefreshTokenService;
import com.derkaiser.auth.util.RateLimitUtils;
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
    private final RefreshTokenRepository refreshTokenRepository; // ⚠️ NUEVO
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final RateLimitUtils rateLimitUtils;

    @Value("${app.frontend.url:http://localhost:9000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional
    public void passwordResetRequest(String email) {
        log.info("Solicitud de reset de password para email: {}", email);

        // Aplicar rate limiting para evitar envío masivo de emails
        String rateLimitKey = "forgot-password:" + email.toLowerCase();
        if (!rateLimitUtils.isAllowed(rateLimitKey)) {
            log.warn("Rate limit alcanzado para forgot-password para email: {}", email);
            // Retornar sin error para no revelar si el email existe
            // Si queremos manejar explícitamente el rate limit, podríamos lanzar una excepción
            // pero según el patrón de seguridad, es mejor no revelar la información
            return;
        }

        // Buscar usuario
        Optional<UserEntity> userOptional = userEntityRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            passwordResetTokenRepository.deleteByUserEntity(user);

            PasswordResetToken resetToken = PasswordResetToken.create(user);
            passwordResetTokenRepository.save(resetToken);

            log.info("Token de reset creado para usuario: {}", user.getEmail());

            // Construir URL de reset
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken.getToken();

            try {
                sendPasswordEmailReset(user.getEmail(), user.getFirstName(), resetUrl);
                log.info("Email de reset enviado exitosamente a: {}", user.getEmail());

                // Registro exitoso - limpiar el contador de intentos fallidos
                rateLimitUtils.recordSuccess("forgot-password:" + email.toLowerCase());
            } catch (Exception e) {
                log.error("Error al enviar email de reset a {}: {}", user.getEmail(), e.getMessage());
                // No lanzar excepción para no revelar si el email existe
            }
        } else {
            // Aunque el email no exista, registramos el intento para prevenir enumeración
            rateLimitUtils.recordFailure("forgot-password:" + email.toLowerCase());
            log.info("Email no encontrado, pero retornando success por seguridad: {}", email);
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

        // Eliminar token después de usarlo
        passwordResetTokenRepository.delete(resetToken);

        // CRÍTICO: Invalidar todos los refresh tokens (cerrar todas las sesiones)
        // Como ahora es @OneToOne, eliminamos el token de refresh asociado al usuario
        refreshTokenRepository.deleteByUserEntity(user);

        log.info("Password reseteado exitosamente para usuario: {} y sesiones cerradas", user.getEmail());


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
