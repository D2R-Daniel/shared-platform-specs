package com.platform.sdk.tenants;

import com.platform.sdk.common.ApiException;

/**
 * Base exception for tenant-related errors.
 */
public class TenantException extends ApiException {
    public TenantException(String message) {
        super(message);
    }

    public TenantException(String message, int statusCode) {
        super(message, statusCode);
    }

    public TenantException(String message, int statusCode, String errorCode) {
        super(message, statusCode, errorCode);
    }

    public TenantException(String message, Throwable cause) {
        super(message, cause);
    }
}
