package com.marutixchange.payment_service.exception;

import com.marutixchange.payment_service.dto.ApiResponse;
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

    @ExceptionHandler(
            PaymentNotFoundException.class)
    public ResponseEntity handleNotFound(
            PaymentNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        ex.getMessage()));
    }

    @ExceptionHandler(
            PaymentAlreadyDoneException.class)
    public ResponseEntity handleAlreadyDone(
            PaymentAlreadyDoneException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        ex.getMessage()));
    }

    @ExceptionHandler(
            PaymentValidationException.class)
    public ResponseEntity handleValidationEx(
            PaymentValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ex.getMessage()));
    }

    @ExceptionHandler(
            EscrowNotFoundException.class)
    public ResponseEntity handleEscrowNotFound(
            EscrowNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        ex.getMessage()));
    }

    @ExceptionHandler(
            RefundNotEligibleException.class)
    public ResponseEntity handleRefundNotEligible(
            RefundNotEligibleException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ex.getMessage()));
    }

    @ExceptionHandler(
            IllegalStateException.class)
    public ResponseEntity handleIllegalState(
            IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ex.getMessage()));
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class)
    public ResponseEntity handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors =
                new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String field =
                            ((FieldError) error).getField();
                    errors.put(field,
                            error.getDefaultMessage());
                });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .statusCode(400)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleGeneric(
            Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred"));
    }
}