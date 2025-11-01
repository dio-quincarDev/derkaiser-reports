package com.derkaiser.auth.service.impl;

import com.derkaiser.auth.commons.dto.response.UserResponse;
import com.derkaiser.auth.repository.UserEntityRepository;
import com.derkaiser.auth.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserEntityRepository userEntityRepository;

    @Override
    public List<UserResponse> listUsers() {
        return userEntityRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        return userEntityRepository.findByEmail(email)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }
}
