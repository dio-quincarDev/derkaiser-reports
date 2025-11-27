package com.derkaiser.exceptions.auth;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public RateLimitExceededException() {
        super("Límite de intentos alcanzado. Inténtalo de nuevo más tarde.");
    }
}