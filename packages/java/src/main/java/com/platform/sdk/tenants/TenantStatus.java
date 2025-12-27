package com.platform.sdk.tenants;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Tenant status values.
 */
public enum TenantStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    SUSPENDED("suspended"),
    PENDING("pending");

    private final String value;

    TenantStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TenantStatus fromValue(String value) {
        for (TenantStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown tenant status: " + value);
    }
}
