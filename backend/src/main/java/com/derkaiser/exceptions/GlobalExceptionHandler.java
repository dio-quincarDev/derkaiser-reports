package com.derkaiser.exceptions;

import com.derkaiser.auth.commons.dto.response.ErrorResponse;
import com.derkaiser.exceptions.auth.DuplicateEmailException;
import com.derkaiser.exceptions.auth.InvalidJwtTokenException;
import com.derkaiser.exceptions.auth.MissingJwtClaimException;
import com.derkaiser.exceptions.auth.UserNotVerifiedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException ex) {
        ErrorResponse errorResponse = new ErrorResponse("DUPLICATE_EMAIL", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwtTokenException(InvalidJwtTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse("INVALID_JWT_TOKEN", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MissingJwtClaimException.class)
    public ResponseEntity<ErrorResponse> handleMissingJwtClaimException(MissingJwtClaimException ex) {
        ErrorResponse errorResponse = new ErrorResponse("MISSING_JWT_CLAIM", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotVerifiedException(UserNotVerifiedException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_NOT_VERIFIED", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledUserException(org.springframework.security.authentication.DisabledException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_ACCOUNT_DISABLED", "Tu cuenta no está activa o verificada");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(com.derkaiser.exceptions.auth.RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(com.derkaiser.exceptions.auth.RateLimitExceededException ex) {
        ErrorResponse errorResponse = new ErrorResponse("RATE_LIMIT_EXCEEDED", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(com.derkaiser.exceptions.auth.UserInactiveException.class)
    public ResponseEntity<ErrorResponse> handleUserInactiveException(com.derkaiser.exceptions.auth.UserInactiveException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_INACTIVE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Add more exception handlers as needed

    // Generic exception handler for any unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Evitar manejar excepciones específicas de endpoints de OpenAPI
        String exceptionMessage = ex.getMessage() != null ? ex.getMessage() : "";
        if (exceptionMessage.contains("api-docs") || exceptionMessage.contains("v3/api-docs") ||
            exceptionMessage.contains("swagger") || exceptionMessage.contains("openapi")) {
            // No manejar excepciones relacionadas con OpenAPI, dejar que SpringDoc las maneje
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", "Ha ocurrido un error inesperado.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
