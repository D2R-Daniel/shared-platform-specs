package com.platform.sdk.teams;

import com.platform.sdk.common.ApiException;

/**
 * Base exception for team-related errors.
 */
public class TeamException extends ApiException {
    public TeamException(String message) {
        super(message);
    }

    public TeamException(String message, int statusCode) {
        super(message, statusCode);
    }

    public TeamException(String message, int statusCode, String errorCode) {
        super(message, statusCode, errorCode);
    }

    public TeamException(String message, Throwable cause) {
        super(message, cause);
    }
}
