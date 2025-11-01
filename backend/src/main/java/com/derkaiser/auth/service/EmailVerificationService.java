package com.derkaiser.auth.service;

import com.derkaiser.auth.commons.model.entity.UserEntity;

public interface EmailVerificationService {

    void createAndSendVerificationEmail(UserEntity user);

    void emailVerification(String token);

    void resendVerificationEmail(String email);

}
