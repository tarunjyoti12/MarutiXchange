package com.marutixchange.apigateway.exception;

/**
 * Custom exception class for API Gateway specific errors.
 * This exception will be used for gateway-level error scenarios.
 */
public class ApiGatewayException extends RuntimeException {

    public ApiGatewayException(String message) {
        super(message);
    }

    public ApiGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
