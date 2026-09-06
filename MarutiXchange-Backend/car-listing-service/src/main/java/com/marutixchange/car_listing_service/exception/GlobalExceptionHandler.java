package com.marutixchange.car_listing_service.exception;

import com.marutixchange.car_listing_service.dto.ApiResponse;
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

    @ExceptionHandler(CarListingNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            CarListingNotFoundException ex) {

        log.error("Not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
    }

    @ExceptionHandler(InvalidListingException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidListing(
            InvalidListingException ex) {

        log.error("Invalid listing: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedAccessException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String field = ((FieldError) error).getField();
                    errors.put(field, error.getDefaultMessage());
                });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(
            Exception ex) {

        log.error("Unexpected error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("An unexpected error occurred")
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build());
    }
}