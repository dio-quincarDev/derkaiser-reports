package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.dto.request.LoginRequest;
import com.derkaiser.auth.commons.dto.request.UserEntityRequest;
import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.enums.UserRole;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.impl.AuthServiceImpl;
import com.derkaiser.exceptions.auth.DuplicateEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity user;
    private UserEntityRequest userEntityRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .password("password")
                .role(UserRole.USER)
                .build();

        userEntityRequest = UserEntityRequest.builder()
                .email("test@test.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .cargo("Developer")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@test.com")
                .password("password")
                .build();
    }

    @Test
    void createUser_shouldCreateUserAndSendVerificationEmail() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(user);
        doNothing().when(emailVerificationService).createAndSendVerificationEmail(any(UserEntity.class));

        TokenResponse response = authService.createUser(userEntityRequest);

        assertNotNull(response);
        assertEquals("Usuario registrado. Revisa tu email para verificar tu cuenta.", response.getMessage());
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());

        verify(userEntityRepository, times(1)).findByEmail(userEntityRequest.getEmail());
        verify(userEntityRepository, times(1)).save(any(UserEntity.class));
        verify(emailVerificationService, times(1)).createAndSendVerificationEmail(user);
    }

    @Test
    void createUser_shouldThrowDuplicateEmailException() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(DuplicateEmailException.class, () -> authService.createUser(userEntityRequest));

        verify(userEntityRepository, times(1)).findByEmail(userEntityRequest.getEmail());
        verify(userEntityRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void login_shouldReturnTokenResponse() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(any(UserEntity.class))).thenReturn("refreshToken");

        TokenResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateAccessToken(user.getEmail(), user.getRole().name());
        verify(refreshTokenService, times(1)).createRefreshToken(user);
    }

    // Edge Cases

    @Test
    void createUser_withNullUserEntityRequest_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> authService.createUser(null));
    }

    @Test
    void login_withNullLoginRequest_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> authService.login(null));
    }

    @Test
    void login_withInvalidCredentials_shouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> authService.login(loginRequest));
    }

    // Robust/Common Tests

    @Test
    void getCurrentUserEmail_whenAuthenticated_shouldReturnEmail() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        String email = authService.getCurrentUserEmail();

        assertEquals(user.getEmail(), email);
    }

    @Test
    void getCurrentUserEmail_whenNotAuthenticated_shouldThrowException() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(IllegalStateException.class, () -> authService.getCurrentUserEmail());
    }
}