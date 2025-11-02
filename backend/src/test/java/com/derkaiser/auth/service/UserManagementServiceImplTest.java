package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.dto.response.UserResponse;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.enums.UserRole;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.impl.UserManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @InjectMocks
    private UserManagementServiceImpl userManagementService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .password("password")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void listUsers_shouldReturnUserList() {
        when(userEntityRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<UserResponse> response = userManagementService.listUsers();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(user.getEmail(), response.get(0).getEmail());

        verify(userEntityRepository, times(1)).findAll();
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserResponse response = userManagementService.getUserByEmail("test@test.com");

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());

        verify(userEntityRepository, times(1)).findByEmail("test@test.com");
    }

    @Test
    void getUserByEmail_shouldThrowExceptionWhenUserNotFound() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userManagementService.getUserByEmail("test@test.com"));

        verify(userEntityRepository, times(1)).findByEmail("test@test.com");
    }

    // Edge Cases

    @Test
    void listUsers_whenNoUsers_shouldReturnEmptyList() {
        when(userEntityRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponse> response = userManagementService.listUsers();

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getUserByEmail_withNullEmail_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> userManagementService.getUserByEmail(null));
    }

    @Test
    void getUserByEmail_withEmptyEmail_shouldThrowException() {
        when(userEntityRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userManagementService.getUserByEmail(""));
    }

    // Robust/Common Tests

    @Test
    void listUsers_shouldReturnMultipleUsers() {
        UserEntity user2 = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test2@test.com")
                .password("password2")
                .role(UserRole.ADMIN)
                .build();
        when(userEntityRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserResponse> response = userManagementService.listUsers();

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void listUsers_withDifferentRoles_shouldReturnCorrectRoles() {
        UserEntity adminUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("admin@test.com")
                .password("password")
                .role(UserRole.ADMIN)
                .build();
        when(userEntityRepository.findAll()).thenReturn(List.of(user, adminUser));

        List<UserResponse> responses = userManagementService.listUsers();

        assertEquals(2, responses.size());
        assertEquals(UserRole.USER, responses.get(0).getRole());
        assertEquals(UserRole.ADMIN, responses.get(1).getRole());
    }
}
