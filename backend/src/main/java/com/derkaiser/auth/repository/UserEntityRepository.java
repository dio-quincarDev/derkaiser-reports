package com.derkaiser.auth.repository;

import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.enums.UserRole;

import java.util.Optional;

public interface UserEntityRepository {

    Optional<UserEntity>findByEmail(String email);

    boolean existsByRole(UserRole role);
}
