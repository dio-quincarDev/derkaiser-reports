package com.derkaiser.auth.service;

public interface PasswordResetService {

    void passwordResetRequest(String email);

    void passwordReset(String token, String newPassword);



}
