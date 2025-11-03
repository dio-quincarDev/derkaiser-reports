package com.derkaiser.exceptions.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un token JWT es inválido, ya sea porque está malformado,
 * ha expirado o tiene una firma incorrecta.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
