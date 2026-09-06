package com.marutixchange.bidding_service.exception;

import com.marutixchange.bidding_service.dto.ApiResponse;
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

    // 🔴 NOT FOUND
    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(AuctionNotFoundException ex) {

        log.warn("Auction not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    // 🔴 BID TOO LOW
    @ExceptionHandler(BidTooLowException.class)
    public ResponseEntity<ApiResponse<Void>> handleBidTooLow(BidTooLowException ex) {

        log.warn("Bid validation failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), 400));
    }

    // 🔴 AUCTION ENDED
    @ExceptionHandler(AuctionEndedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuctionEnded(AuctionEndedException ex) {

        log.warn("Auction ended: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), 400));
    }

    // 🔴 UNAUTHORIZED
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedAccessException ex) {

        log.warn("Unauthorized access: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), 403));
    }

    // 🔴 VALIDATION ERROR
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();

            // ✅ FIX: handle multiple errors per field
            errors.merge(field, error.getDefaultMessage(),
                    (oldVal, newVal) -> oldVal + ", " + newVal);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .statusCode(400)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    // 🔴 GENERIC ERROR (SAFE)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneric(Exception ex) {

        log.error("Unexpected error occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Something went wrong. Please try again.",
                        500
                ));
    }
}