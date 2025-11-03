package com.derkaiser.exceptions.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un claim esperado no se encuentra en un token JWT.
 * Esto generalmente indica un token malformado o uno que no es apropiado para la operación solicitada.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class MissingJwtClaimException extends RuntimeException {
    public MissingJwtClaimException(String message) {
        super(message);
    }
}
