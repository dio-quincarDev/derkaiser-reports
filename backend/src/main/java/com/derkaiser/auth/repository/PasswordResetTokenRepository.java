package com.derkaiser.auth.repository;

import com.derkaiser.auth.commons.model.entity.PasswordResetToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;

import java.util.Optional;

public interface PasswordResetTokenRepository {

    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUsuario(UserEntity userEntity);
}
