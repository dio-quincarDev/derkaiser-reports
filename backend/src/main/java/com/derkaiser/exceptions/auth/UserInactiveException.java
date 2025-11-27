package com.derkaiser.exceptions.auth;

public class UserInactiveException extends RuntimeException {

    public UserInactiveException(String message) {
        super(message);
    }

    public UserInactiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserInactiveException() {
        super("La cuenta de usuario está inactiva");
    }
}