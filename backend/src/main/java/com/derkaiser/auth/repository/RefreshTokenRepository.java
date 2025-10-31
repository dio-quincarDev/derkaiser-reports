package com.derkaiser.auth.repository;

import com.derkaiser.auth.commons.model.entity.RefreshToken;
import com.derkaiser.auth.commons.model.entity.UserEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByToken(String token);
    void deleteByUsuario(UserEntity userEntity);
    void deleteAllByUsuario(UserEntity userEntity); // Para password reset
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
