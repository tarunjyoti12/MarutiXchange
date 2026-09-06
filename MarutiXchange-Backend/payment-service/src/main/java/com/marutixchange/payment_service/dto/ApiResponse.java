package com.marutixchange.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int statusCode;

    @Builder.Default
    private LocalDateTime timestamp =
            LocalDateTime.now();

    public static <T> ApiResponse<T> success(
            String message, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(message)
                .data(data).statusCode(200).build();
    }

    public static <T> ApiResponse<T> error(
            String message) {
        return ApiResponse.<T>builder()
                .success(false).message(message)
                .statusCode(400).build();
    }
}