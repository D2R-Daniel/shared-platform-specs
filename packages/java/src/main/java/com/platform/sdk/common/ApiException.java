package com.platform.sdk.common;

/**
 * Base exception for SDK API errors.
 */
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorCode = null;
    }

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = null;
    }

    public ApiException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorCode = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
