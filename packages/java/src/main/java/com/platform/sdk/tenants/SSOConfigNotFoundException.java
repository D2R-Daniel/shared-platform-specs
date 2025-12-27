package com.platform.sdk.tenants;

/**
 * Exception thrown when SSO configuration is not found.
 */
public class SSOConfigNotFoundException extends TenantException {
    private final String tenantId;

    public SSOConfigNotFoundException(String tenantId) {
        super("SSO configuration not found for tenant: " + tenantId, 404, "SSO_CONFIG_NOT_FOUND");
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
