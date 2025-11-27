package com.derkaiser.auth.controller;

import com.derkaiser.auth.commons.dto.request.LoginRequest;
import com.derkaiser.auth.commons.dto.request.RefreshTokenRequest;
import com.derkaiser.auth.commons.dto.request.UserEntityRequest;
import com.derkaiser.auth.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private UserEntityRequest validUserRequest;

    @BeforeEach
    void setUp() {
        validUserRequest = UserEntityRequest.builder()
                .email("testuser@example.com")
                .password("TestPassword123!")
                .firstName("Test")
                .lastName("User")
                .cargo("Developer")
                .build();
    }

    @Test
    void registerUser_happyPath_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuario registrado. Revisa tu email para verificar tu cuenta."))
                .andExpect(jsonPath("$.access_token").value((String) null))
                .andExpect(jsonPath("$.refresh_token").value((String) null));
    }

    @Test
    void login_withUnverifiedUser_shouldReturnUnauthorized() throws Exception {
        // Primero registramos al usuario
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isCreated());

        // Luego intentamos login, lo cual debería fallar porque el email no está verificado
        LoginRequest loginRequest = LoginRequest.builder()
                .email("testuser@example.com")
                .password("TestPassword123!")
                .build();

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("USER_ACCOUNT_DISABLED"))
                .andExpect(jsonPath("$.message").value("Tu cuenta no está activa o verificada"));
    }

    @Test
    void logout_withValidToken_ShouldReturnSuccess() throws Exception {
        // Test de logout con un refresh token falso (esto debería funcionar igual)
        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("some-fake-refresh-token")
                .build();

        mockMvc.perform(post("/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada exitosamente."));
    }
}