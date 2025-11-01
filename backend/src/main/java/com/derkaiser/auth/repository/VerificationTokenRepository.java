package com.derkaiser.auth.repository;

import com.derkaiser.auth.commons.model.entity.UserEntity;
import com.derkaiser.auth.commons.model.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID>{

    Optional<VerificationToken> findByToken(String token);
    void deleteByUserEntity(UserEntity userEntity);
}
