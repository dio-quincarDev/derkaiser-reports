package com.derkaiser.auth.repository;

import com.derkaiser.auth.commons.model.entity.PasswordResetToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserEntity(UserEntity userEntity);
}
