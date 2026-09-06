package com.marutixchange.user_service.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value);
    }
}