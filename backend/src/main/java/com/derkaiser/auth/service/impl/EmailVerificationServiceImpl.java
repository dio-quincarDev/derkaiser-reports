package com.derkaiser.auth.service.impl;

import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.entity.VerificationToken;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.repository.VerificationTokenRepository;
import com.derkaiser.auth.service.EmailVerificationService;
import com.derkaiser.exceptions.auth.UserNotVerifiedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserEntityRepository userEntityRepository;
    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional
    public void createAndSendVerificationEmail(UserEntity user) {

        log.info("Creando token de verificación para usuario: {}", user.getEmail());

        verificationTokenRepository.deleteByUserEntity(user);
        VerificationToken token = VerificationToken.create(user);
        verificationTokenRepository.save(token);

        log.info("Token de verificación creado con ID: {}", token.getId());

        String verifyUrl = frontendUrl + "/verify?token=" + token.getToken();

        try{
            sendEmailVerification(user.getEmail(), user.getFirstName(), verifyUrl);
            log.info("Email de verificación enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de verificación a {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Error al enviar email de verificación", e);
        }

    }
    // En el método emailVerification
    @Override
    @Transactional
    public void emailVerification(String token) {
        log.info("Intentando verificar email con token: {}", token);

        // Buscar token
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Token de verificación no encontrado: {}", token);
                    return new UserNotVerifiedException("Token de verificación inválido");
                });

        // Validar expiración
        if (verificationToken.isExpired()) {
            log.warn("Intento de usar token expirado: {}", token);
            throw new UserNotVerifiedException("El token ha expirado. Solicita un nuevo email de verificación");
        }

        // Resto del código permanece igual...
        UserEntity user = verificationToken.getUserEntity();
        user.setActive(true);
        user.setVerificatedEmail(true);
        userEntityRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        log.info("Email verificado exitosamente para usuario: {}", user.getEmail());
    }


    @Override
    @Transactional
    public void resendVerificationEmail(String email) {

        log.info("Reenviando email de verificación para: {}", email);

        // Buscar usuario
        UserEntity user = userEntityRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado al reenviar verificación: {}", email);
                    return new UserNotVerifiedException("Usuario no encontrado");
                });

        // Validar que no esté ya verificado
        if (user.getVerificatedEmail()) {
            log.warn("Intento de reenviar verificación a email ya verificado: {}", email);
            throw new UserNotVerifiedException("Este email ya está verificado");
        }

        // Crear y enviar nuevo token
        createAndSendVerificationEmail(user);
    }

    private void sendEmailVerification(String toEmail, String firstName, String verifyUrl) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Verifica tu cuenta - Infoplazas AIP");
        helper.setText(construirMensajeVerificacion(firstName, verifyUrl), true); // true indica que es HTML

        mailSender.send(mimeMessage);
    }

    private String construirMensajeVerificacion(String firstName, String verifyUrl) {
        return String.format("""
                <div style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Hola %s,</h2>
                    <p>Gracias por registrarte en Infoplazas AIP.</p>
                    <p>Por favor, verifica tu cuenta haciendo clic en el siguiente enlace:</p>
                    <p><a href="%s" style="color: #1a73e8; text-decoration: none;">Verificar mi cuenta</a></p>
                    <p>Este enlace expirará en 24 horas.</p>
                    <br>
                    <p>Si no creaste esta cuenta, puedes ignorar este mensaje.</p>
                    <br>
                    <p>Saludos,<br>Equipo de Infoplazas AIP</p>
                </div>
                """, firstName, verifyUrl);
    }

}

