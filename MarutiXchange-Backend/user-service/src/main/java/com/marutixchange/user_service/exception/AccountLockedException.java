package com.marutixchange.user_service.exception;

public class AccountLockedException extends RuntimeException {

    public AccountLockedException() {
        super("Account is locked due to too many failed login attempts. Please try again later.");
    }

    public AccountLockedException(String message) {
        super(message);
    }
}