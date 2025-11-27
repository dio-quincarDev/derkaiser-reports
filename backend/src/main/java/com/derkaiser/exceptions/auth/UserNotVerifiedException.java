package com.derkaiser.exceptions.auth;

public class UserNotVerifiedException extends RuntimeException {

    public UserNotVerifiedException(String message) {
        super(message);
    }

    public UserNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotVerifiedException() {
        super("El usuario no ha verificado su email");
    }
}