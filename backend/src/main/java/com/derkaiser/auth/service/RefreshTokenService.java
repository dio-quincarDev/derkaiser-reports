package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.dto.response.TokenResponse;
import com.derkaiser.auth.commons.model.entity.UserEntity;

public interface RefreshTokenService {

    String createRefreshToken(UserEntity userEntity);

    TokenResponse refreshAccessToken(String refreshToken);

    void deleteByToken(String refreshToken);

    void cleanExpiredTokens();
}
