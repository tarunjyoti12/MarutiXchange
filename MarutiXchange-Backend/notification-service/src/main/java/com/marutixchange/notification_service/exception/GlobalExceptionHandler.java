package com.marutixchange.notification_service.exception;

import com.marutixchange.notification_service.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotificationNotFound(NotificationNotFoundException ex) {
        log.warn("Notification not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ex.getMessage(), "NOTIFICATION_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidNotificationRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(InvalidNotificationRequestException ex) {
        log.warn("Invalid notification request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(ex.getMessage(), "INVALID_NOTIFICATION_REQUEST"));
    }

    @ExceptionHandler(NotificationDeliveryException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeliveryFailure(NotificationDeliveryException ex) {
        log.error("Notification delivery failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(ex.getMessage(), "NOTIFICATION_DELIVERY_FAILED"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .errorCode("VALIDATION_FAILED")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("An unexpected error occurred", "INTERNAL_SERVER_ERROR"));
    }
}