package com.platform.sdk.permissions;

import com.platform.sdk.common.ApiException;

/**
 * Base exception for role-related errors.
 */
public class RoleException extends ApiException {
    public RoleException(String message) {
        super(message);
    }

    public RoleException(String message, int statusCode) {
        super(message, statusCode);
    }

    public RoleException(String message, int statusCode, String errorCode) {
        super(message, statusCode, errorCode);
    }

    public RoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
