package com.platform.sdk.auth;

import com.platform.sdk.common.ApiException;

/**
 * Base exception for authentication errors.
 */
public class AuthException extends ApiException {
    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, int statusCode) {
        super(message, statusCode);
    }

    public AuthException(String message, int statusCode, String errorCode) {
        super(message, statusCode, errorCode);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when a token has expired.
 */
class TokenExpiredException extends AuthException {
    public TokenExpiredException() {
        super("Token has expired", 401, "TOKEN_EXPIRED");
    }

    public TokenExpiredException(String message) {
        super(message, 401, "TOKEN_EXPIRED");
    }
}

/**
 * Exception thrown when a token is invalid.
 */
class InvalidTokenException extends AuthException {
    public InvalidTokenException() {
        super("Invalid token", 401, "INVALID_TOKEN");
    }

    public InvalidTokenException(String message) {
        super(message, 401, "INVALID_TOKEN");
    }
}

/**
 * Exception thrown when credentials are invalid.
 */
class UnauthorizedException extends AuthException {
    public UnauthorizedException() {
        super("Invalid credentials", 401, "UNAUTHORIZED");
    }

    public UnauthorizedException(String message) {
        super(message, 401, "UNAUTHORIZED");
    }
}
