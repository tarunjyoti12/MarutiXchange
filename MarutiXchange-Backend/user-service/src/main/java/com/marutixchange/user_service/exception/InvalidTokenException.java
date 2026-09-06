package com.marutixchange.user_service.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid or expired token. Please request a new one.");
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}