package com.marutixchange.user_service.exception;

public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException() {
        super("Invalid or expired OTP. Please request a new OTP.");
    }

    public InvalidOtpException(String message) {
        super(message);
    }
}