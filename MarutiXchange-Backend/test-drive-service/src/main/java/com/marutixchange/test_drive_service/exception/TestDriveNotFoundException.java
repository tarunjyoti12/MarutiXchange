package com.marutixchange.test_drive_service.exception;

public class TestDriveNotFoundException extends RuntimeException {
    public TestDriveNotFoundException(Long id) {
        super("Test drive not found with id: " + id);
    }
}
