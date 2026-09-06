package com.marutixchange.bidding_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private int statusCode;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(200)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null) // ✅ FIX
                .statusCode(statusCode)
                .build();
    }
}