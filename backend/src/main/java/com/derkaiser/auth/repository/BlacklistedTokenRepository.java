package com.derkaiser.auth.repository;

import com.derkaiser.auth.commons.model.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {

    Optional<BlacklistedToken> findByToken(String token);

    boolean existsByToken(String token);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);
}