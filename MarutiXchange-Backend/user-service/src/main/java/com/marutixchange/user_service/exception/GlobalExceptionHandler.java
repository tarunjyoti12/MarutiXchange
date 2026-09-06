package com.marutixchange.user_service.exception;

import com.marutixchange.user_service.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ================= CUSTOM EXCEPTIONS =================

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(
            UserNotFoundException ex) {

        log.error("User not found: {}", ex.getMessage());

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailExists(
            EmailAlreadyExistsException ex) {

        log.error("Email already exists: {}", ex.getMessage());

        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountLocked(
            AccountLockedException ex) {

        log.warn("Account locked: {}", ex.getMessage());

        return buildResponse(HttpStatus.LOCKED, ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
            InvalidTokenException ex) {

        log.warn("Invalid token: {}", ex.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ================= OPTIONAL (IF YOU ADD THESE CLASSES) =================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex) {

        log.error("Runtime exception: {}", ex.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ================= VALIDATION =================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .statusCode(400)
                        .build());
    }

    // ================= GLOBAL =================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex) {

        log.error("Unexpected error occurred", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again."
        );
    }

    // ================= COMMON METHOD =================

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status,
            String message) {

        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .statusCode(status.value())
                        .build());
    }
}