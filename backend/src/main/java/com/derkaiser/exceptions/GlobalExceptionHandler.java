package com.derkaiser.exceptions;

import com.derkaiser.auth.commons.dto.response.ErrorResponse;
import com.derkaiser.exceptions.auth.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
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

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledUserException(DisabledException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_ACCOUNT_DISABLED", "Tu cuenta no está activa o verificada");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex) {
        ErrorResponse errorResponse = new ErrorResponse("RATE_LIMIT_EXCEEDED", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<ErrorResponse> handleUserInactiveException(UserInactiveException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_INACTIVE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordResetTokenException(InvalidPasswordResetTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse("INVALID_RESET_TOKEN", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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
