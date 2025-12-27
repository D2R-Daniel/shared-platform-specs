package com.platform.sdk.tenants;

import com.platform.sdk.common.ApiException;
import com.platform.sdk.common.HttpClient;

import java.time.Duration;
import java.util.Map;

/**
 * Client for tenant management operations.
 */
public class TenantClient {
    private final HttpClient httpClient;

    private TenantClient(Builder builder) {
        this.httpClient = new HttpClient(builder.baseUrl, builder.timeout);
        if (builder.accessToken != null) {
            this.httpClient.setAccessToken(builder.accessToken);
        }
    }

    /**
     * Set the access token for authenticated requests.
     */
    public void setAccessToken(String accessToken) {
        this.httpClient.setAccessToken(accessToken);
    }

    // Tenant Operations

    /**
     * List tenants with optional filtering and pagination.
     */
    public TenantListResponse list(ListTenantsParams params) throws ApiException {
        Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
        return httpClient.get("/tenants", TenantListResponse.class, queryParams);
    }

    /**
     * List tenants with default parameters.
     */
    public TenantListResponse list() throws ApiException {
        return list(null);
    }

    /**
     * Get a tenant by ID.
     */
    public Tenant get(String tenantId) throws ApiException {
        try {
            return httpClient.get("/tenants/" + tenantId, Tenant.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TenantNotFoundException(tenantId);
            }
            throw e;
        }
    }

    /**
     * Create a new tenant.
     */
    public Tenant create(CreateTenantRequest request) throws ApiException {
        return httpClient.post("/tenants", request, Tenant.class);
    }

    /**
     * Update an existing tenant.
     */
    public Tenant update(String tenantId, UpdateTenantRequest request) throws ApiException {
        try {
            return httpClient.put("/tenants/" + tenantId, request, Tenant.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TenantNotFoundException(tenantId);
            }
            throw e;
        }
    }

    /**
     * Delete a tenant.
     */
    public void delete(String tenantId) throws ApiException {
        try {
            httpClient.delete("/tenants/" + tenantId);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TenantNotFoundException(tenantId);
            }
            throw e;
        }
    }

    /**
     * Update tenant status.
     */
    public Tenant updateStatus(String tenantId, TenantStatus status, String reason) throws ApiException {
        try {
            Map<String, String> body = Map.of(
                    "status", status.getValue(),
                    "reason", reason != null ? reason : ""
            );
            return httpClient.patch("/tenants/" + tenantId + "/status", body, Tenant.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TenantNotFoundException(tenantId);
            }
            throw e;
        }
    }

    // SSO Operations

    /**
     * Get SSO configuration for a tenant.
     */
    public SSOConfig getSSOConfig(String tenantId) throws ApiException {
        try {
            return httpClient.get("/tenants/" + tenantId + "/sso", SSOConfig.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new SSOConfigNotFoundException(tenantId);
            }
            throw e;
        }
    }

    /**
     * Update SSO configuration for a tenant.
     */
    public SSOConfig updateSSOConfig(String tenantId, SSOConfig config) throws ApiException {
        try {
            return httpClient.put("/tenants/" + tenantId + "/sso", config, SSOConfig.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TenantNotFoundException(tenantId);
            }
            throw e;
        }
    }

    /**
     * Delete SSO configuration for a tenant.
     */
    public void deleteSSOConfig(String tenantId) throws ApiException {
        try {
            httpClient.delete("/tenants/" + tenantId + "/sso");
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TenantNotFoundException(tenantId);
            }
            throw e;
        }
    }

    /**
     * Test SSO connection for a tenant.
     */
    public SSOTestResult testSSOConnection(String tenantId) throws ApiException {
        try {
            return httpClient.post("/tenants/" + tenantId + "/sso/test", null, SSOTestResult.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TenantNotFoundException(tenantId);
            }
            throw e;
        }
    }

    /**
     * Trigger SSO sync for a tenant.
     */
    public SSOSyncResult triggerSSOSync(String tenantId) throws ApiException {
        try {
            return httpClient.post("/tenants/" + tenantId + "/sso/sync", null, SSOSyncResult.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TenantNotFoundException(tenantId);
            }
            throw e;
        }
    }

    /**
     * Builder for TenantClient.
     */
    public static class Builder {
        private String baseUrl;
        private String accessToken;
        private Duration timeout = Duration.ofSeconds(30);

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public TenantClient build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            return new TenantClient(this);
        }
    }

    /**
     * SSO test result.
     */
    public static class SSOTestResult {
        private Boolean success;
        private String message;
        private Map<String, Object> details;

        public SSOTestResult() {
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }
    }

    /**
     * SSO sync result.
     */
    public static class SSOSyncResult {
        private String syncId;
        private String status;
        private String startedAt;

        public SSOSyncResult() {
        }

        public String getSyncId() {
            return syncId;
        }

        public void setSyncId(String syncId) {
            this.syncId = syncId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(String startedAt) {
            this.startedAt = startedAt;
        }
    }
}
