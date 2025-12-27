package com.platform.sdk.tenants;

/**
 * Exception thrown when a tenant is not found.
 */
public class TenantNotFoundException extends TenantException {
    private final String tenantId;

    public TenantNotFoundException(String tenantId) {
        super("Tenant not found: " + tenantId, 404, "TENANT_NOT_FOUND");
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
