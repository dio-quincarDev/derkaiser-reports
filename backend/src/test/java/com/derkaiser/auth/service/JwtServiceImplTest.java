package com.derkaiser.auth.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.derkaiser.auth.repository.BlacklistedTokenRepository;
import com.derkaiser.auth.service.impl.JwtServiceImpl;

import com.derkaiser.exceptions.auth.InvalidJwtTokenException;
import com.derkaiser.exceptions.auth.MissingJwtClaimException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    private JwtServiceImpl jwtService;
    private final String testSecret = "thisisatestsecretkeythatissufficientlylongforjwt"; // Length >= 32
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(testSecret, blacklistedTokenRepository);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 10000L); // 10 seconds for testing
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 600000L); // 10 minutes for testing
        this.secretKey = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void constructor_shouldInitializeWithValidSecret() {
        assertNotNull(jwtService);
    }

    @Test
    void constructor_shouldThrowExceptionWithShortSecret() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new JwtServiceImpl("short", blacklistedTokenRepository));
        assertEquals("La clave secreta de JWT debe tener al menos 32 caracteres.", exception.getMessage());
    }

    @Test
    void generateAccessToken_shouldGenerateValidToken() {
        String email = "test@example.com";
        String role = "USER";
        String token = jwtService.generateAccessToken(email, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtService.getClaims(token);
        assertEquals(email, claims.getSubject());
        assertEquals(role, claims.get("role"));
        assertEquals("access", claims.get("type"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateAccessToken_shouldNormalizeRole() {
        String email = "test@example.com";
        String role = "ROLE_ADMIN";
        String token = jwtService.generateAccessToken(email, role);

        Claims claims = jwtService.getClaims(token);
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void generateRefreshToken_shouldGenerateValidToken() {
        String email = "test@example.com";
        String token = jwtService.generateRefreshToken(email);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtService.getClaims(token);
        assertEquals(email, claims.getSubject());
        assertEquals("refresh", claims.get("type"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtService.generateAccessToken("test@example.com", "USER");
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() throws InterruptedException {
        // Temporarily set a very short expiration for this test
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 100L); // 100ms for this test
        String token = jwtService.generateAccessToken("test@example.com", "USER");
        TimeUnit.MILLISECONDS.sleep(150); // Wait for token to expire
        assertFalse(jwtService.validateToken(token));

        // Restore normal expiration time
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 10000L);
    }

    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        Logger logger = (Logger) LoggerFactory.getLogger(JwtServiceImpl.class);
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
        try {
            assertFalse(jwtService.validateToken("malformed.token.string"));
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    @Test
    void validateToken_shouldReturnFalseForTokenWithInvalidSignature() {
        // Generate a token with a different secret key
        SecretKey otherSecretKey = Keys.hmacShaKeyFor("anothersecretkeythatissufficientlylongforjwt".getBytes(StandardCharsets.UTF_8));
        String invalidSignatureToken = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(otherSecretKey)
                .compact();

        assertFalse(jwtService.validateToken(invalidSignatureToken));
    }

    @Test
    void getClaims_shouldReturnClaimsForValidToken() {
        String email = "test@example.com";
        String role = "USER";
        String token = jwtService.generateAccessToken(email, role);

        Claims claims = jwtService.getClaims(token);
        assertNotNull(claims);
        assertEquals(email, claims.getSubject());
        assertEquals(role, claims.get("role"));
    }

    @Test
    void getClaims_shouldThrowExceptionForMalformedToken() {
        Logger logger = (Logger) LoggerFactory.getLogger(JwtServiceImpl.class);
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
        try {
            InvalidJwtTokenException exception = assertThrows(InvalidJwtTokenException.class,
                    () -> jwtService.getClaims("malformed.token.string"));
            assertTrue(exception.getMessage().contains("Token JWT inválido o expirado"));
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    @Test
    void getClaims_shouldThrowExceptionForExpiredToken() throws InterruptedException {
        // Temporarily set a very short expiration for this test
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 100L); // 100ms for this test
        String token = jwtService.generateAccessToken("test@example.com", "USER");
        TimeUnit.MILLISECONDS.sleep(150); // Wait for token to expire

        Logger logger = (Logger) LoggerFactory.getLogger(JwtServiceImpl.class);
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.OFF);

        try {
            InvalidJwtTokenException exception = assertThrows(InvalidJwtTokenException.class,
                    () -> jwtService.getClaims(token));
            assertTrue(exception.getMessage().contains("Token JWT inválido o expirado"));
            assertTrue(exception.getCause() instanceof ExpiredJwtException);
        } finally {
            logger.setLevel(originalLevel);
        }

        // Restore normal expiration time
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 10000L);
    }

    @Test
    void isExpired_shouldReturnFalseForNonExpiredToken() {
        String token = jwtService.generateAccessToken("test@example.com", "USER");
        assertFalse(jwtService.isExpired(token));
    }

    @Test
    void isExpired_shouldReturnTrueForExpiredToken() throws InterruptedException {
        // Temporarily set a very short expiration for this test
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 100L); // 100ms for this test
        String token = jwtService.generateAccessToken("test@example.com", "USER");
        TimeUnit.MILLISECONDS.sleep(150); // Wait for token to expire
        assertTrue(jwtService.isExpired(token));

        // Restore normal expiration time
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 10000L);
    }

    @Test
    void isExpired_shouldReturnTrueForMalformedToken() {
        Logger logger = (Logger) LoggerFactory.getLogger(JwtServiceImpl.class);
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
        try {
            assertTrue(jwtService.isExpired("malformed.token.string"));
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        String role = "ADMIN";
        String token = jwtService.generateAccessToken("test@example.com", role);
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String email = "test@example.com";
        String token = jwtService.generateAccessToken(email, "USER");
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void extractRole_shouldThrowExceptionIfRoleIsMissing() {
        // Generate a token without a role claim
        String tokenWithoutRole = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(secretKey)
                .compact();

        MissingJwtClaimException exception = assertThrows(MissingJwtClaimException.class,
                () -> jwtService.extractRole(tokenWithoutRole));
        assertEquals("El claim 'role' es requerido en el token", exception.getMessage());
    }

    @Test
    void extractEmail_shouldThrowExceptionIfSubjectIsMissing() {
        // Generate a token without a subject claim
        String tokenWithoutSubject = Jwts.builder()
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(secretKey)
                .compact();

        MissingJwtClaimException exception = assertThrows(MissingJwtClaimException.class,
                () -> jwtService.extractEmail(tokenWithoutSubject));
        assertEquals("El claim 'subject' (email) es requerido en el token", exception.getMessage());
    }

    @Test
    void validateToken_shouldReturnFalseForBlacklistedToken() {
        String token = jwtService.generateAccessToken("test@example.com", "USER");

        // Mock that the token is blacklisted
        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(true);

        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnTrueForValidNonBlacklistedToken() {
        String token = jwtService.generateAccessToken("test@example.com", "USER");

        // Mock that the token is not blacklisted
        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(false);

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void isTokenBlacklisted_shouldReturnTrueWhenTokenExistsInRepository() {
        String token = jwtService.generateAccessToken("test@example.com", "USER");

        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(true);

        assertTrue(jwtService.isTokenBlacklisted(token));
    }

    @Test
    void isTokenBlacklisted_shouldReturnFalseWhenTokenDoesNotExistInRepository() {
        String token = jwtService.generateAccessToken("test@example.com", "USER");

        when(blacklistedTokenRepository.existsByToken(token)).thenReturn(false);

        assertFalse(jwtService.isTokenBlacklisted(token));
    }

    @Test
    void blacklistToken_shouldSaveBlacklistedTokenToRepository() {
        String token = jwtService.generateAccessToken("test@example.com", "USER");

        // Whitelist the method calls to avoid actual execution during test
        when(blacklistedTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        jwtService.blacklistToken(token, "ACCESS");

        verify(blacklistedTokenRepository, times(1)).save(any());
    }
}