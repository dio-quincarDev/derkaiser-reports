package com.derkaiser.auth.service.impl;

import com.derkaiser.auth.util.RateLimitUtils;
import com.derkaiser.exceptions.auth.RateLimitExceededException;
import com.derkaiser.exceptions.auth.UserNotVerifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.derkaiser.auth.commons.dto.request.LoginRequest;
import com.derkaiser.auth.commons.dto.request.UserEntityRequest;
import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.enums.UserRole;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.AuthService;
import com.derkaiser.auth.service.EmailVerificationService;
import com.derkaiser.auth.service.JwtService;
import com.derkaiser.auth.service.RefreshTokenService;
import com.derkaiser.exceptions.auth.DuplicateEmailException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder passWordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService; // ⚠️ NUEVO
    private final RefreshTokenService refreshTokenService; // ⚠️ NUEVO
    private final RateLimitUtils rateLimitUtils;

    @Override
    public TokenResponse createUser(@Valid UserEntityRequest userEntityRequest) {
        log.info("Intentando crear usuario para email: {}", userEntityRequest.getEmail());

        // Aplicar rate limiting para registro por IP (prevención de spam de registros)
        String clientIp = getCurrentRequestIp(); // Obtener IP del cliente actual
        if (clientIp != null) {
            String rateLimitKey = "register:" + clientIp;
            if (!rateLimitUtils.isAllowed(rateLimitKey)) {
                log.warn("Rate limit alcanzado para registro desde IP: {}", clientIp);
                throw new RateLimitExceededException("Demasiados intentos de registro. Inténtalo de nuevo más tarde.");
            }
        }

        if (userEntityRepository.findByEmail(userEntityRequest.getEmail()).isPresent()) {
            log.warn("Intento de crear usuario con email existente: {}", userEntityRequest.getEmail());
            throw new DuplicateEmailException("El email ya esta registrado");
        }

        UserEntity userToSave = mapToEntity(userEntityRequest, UserRole.USER);
        UserEntity userCreated = userEntityRepository.save(userToSave);
        log.info("Usuario creado exitosamente con ID: {}", userCreated.getId());

        // ⚠️ NUEVO: Enviar email de verificación
        try {
            emailVerificationService.createAndSendVerificationEmail(userCreated);
            log.info("Email de verificación enviado a: {}", userCreated.getEmail());

            // Registro exitoso - limpiar el contador de intentos fallidos
            if (clientIp != null) {
                rateLimitUtils.recordSuccess("register:" + clientIp);
            }
        } catch (Exception e) {
            log.error("Error enviando email de verificación: {}", e.getMessage());
            // No fallar el registro si el email falla
        }

        // ⚠️ AJUSTE: No retornar token aún (debe verificar email primero)
        return TokenResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .message("Usuario registrado. Revisa tu email para verificar tu cuenta.")
                .build();
    }

    // Método para obtener la IP del cliente actual
    private String getCurrentRequestIp() {
        try {
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                var request = ((ServletRequestAttributes) requestAttributes).getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                // Manejar listas de IPs en X-Forwarded-For
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener la IP del cliente: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        log.info("Intentando login para usuario: {}", loginRequest.getEmail());

        // Aplicar rate limiting basado en email para evitar fuerza bruta
        String rateLimitKey = "login:" + loginRequest.getEmail().toLowerCase();
        if (!rateLimitUtils.isAllowed(rateLimitKey)) {
            long remainingAttempts = rateLimitUtils.getRemainingAttempts(rateLimitKey);
            String message = String.format("Demasiados intentos de inicio de sesión. Inténtalo de nuevo en %d segundos.",
                remainingAttempts <= 0 ? 300 : 300); // Si ya no hay intentos, mostrar tiempo de espera
            log.warn("Rate limit alcanzado para email: {}", loginRequest.getEmail());
            throw new RateLimitExceededException(message);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserEntity user = (UserEntity) authentication.getPrincipal();

            // Verificar si el usuario ha verificado su email
            if (!user.getVerificatedEmail()) {
                log.warn("Usuario no verificado intentando iniciar sesión: {}", user.getEmail());
                throw new UserNotVerifiedException("Tu cuenta no está verificada. Por favor revisa tu email para verificarla.");
            }

            // Registro exitoso - limpiar el contador de intentos fallidos
            rateLimitUtils.recordSuccess("login:" + loginRequest.getEmail().toLowerCase());

            // ⚠️ AJUSTE: Generar access + refresh tokens
            String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
            String refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("Login exitoso para usuario: {}", user.getEmail());

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(900_000L) // 15 minutos en milisegundos
                    .build();
        } catch (Exception e) {
            // Registro de intento fallido
            rateLimitUtils.recordFailure("login:" + loginRequest.getEmail().toLowerCase());
            log.warn("Intento de inicio de sesión fallido para usuario: {}", loginRequest.getEmail());
            throw e; // Volver a lanzar la excepción para que Spring Security la maneje
        }
    }



    // ⚠️ NUEVO MÉTODO
    @Override
    public void logout(String accessToken, String refreshToken) {
        log.info("Procesando logout para token de acceso y refresco");

        // Añadir el token de acceso a la lista negra
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            try {
                jwtService.blacklistToken(accessToken, "ACCESS");
                log.info("Token de acceso añadido a la lista negra");
            } catch (Exception e) {
                log.warn("Error al añadir token de acceso a la lista negra: {}", e.getMessage());
            }
        }

        // Añadir el token de refresco a la lista negra y eliminarlo de la base de datos
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            try {
                refreshTokenService.deleteByToken(refreshToken);
                log.info("Token de refresco añadido a la lista negra y eliminado de la base de datos");
            } catch (Exception e) {
                log.warn("Error al procesar token de refresco: {}", e.getMessage());
            }
        }

        log.info("Logout completado exitosamente");
    }

    // ⚠️ NUEVO MÉTODO
    @Override
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserEntity) {
            return ((UserEntity) authentication.getPrincipal()).getEmail();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    private UserEntity mapToEntity(UserEntityRequest userEntityRequest, UserRole role) {
        return UserEntity.builder()
                .email(userEntityRequest.getEmail())
                .password(passWordEncoder.encode(userEntityRequest.getPassword()))
                .firstName(userEntityRequest.getFirstName())
                .lastName(userEntityRequest.getLastName())
                .role(role)
                .active(true) // Un nuevo usuario debe estar activo, solo pendiente de verificación
                .verificatedEmail(false) // ⚠️ NUEVO: Email no verificado
                .cargo(userEntityRequest.getCargo()) // ⚠️ NUEVO: Agregar cargo
                .build();
    }
}