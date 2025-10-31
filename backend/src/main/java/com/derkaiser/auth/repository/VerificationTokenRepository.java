package com.derkaiser.auth.repository;

import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.entity.VerificationToken;

import java.util.Optional;

public interface VerificationTokenRepository {

    Optional<VerificationToken> findByToken(String token);
    void deleteByUsuario(UserEntity userEntity);
}
