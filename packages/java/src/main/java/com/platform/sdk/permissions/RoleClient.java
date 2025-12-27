package com.platform.sdk.permissions;

import com.platform.sdk.common.ApiException;
import com.platform.sdk.common.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client for role and permission operations.
 */
public class RoleClient {
    private final HttpClient httpClient;

    private RoleClient(Builder builder) {
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

    // Role CRUD Operations

    /**
     * List roles with optional filtering and pagination.
     */
    public RoleListResponse list(ListRolesParams params) throws ApiException {
        Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
        return httpClient.get("/roles", RoleListResponse.class, queryParams);
    }

    /**
     * List roles with default parameters.
     */
    public RoleListResponse list() throws ApiException {
        return list(null);
    }

    /**
     * Get a role by ID.
     */
    public Role get(String roleId) throws ApiException {
        try {
            return httpClient.get("/roles/" + roleId, Role.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new RoleNotFoundException(roleId);
            }
            throw e;
        }
    }

    /**
     * Get a role by slug.
     */
    public Role getBySlug(String slug) throws ApiException {
        try {
            return httpClient.get("/roles/slug/" + slug, Role.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new RoleNotFoundException(slug);
            }
            throw e;
        }
    }

    /**
     * Create a new role.
     */
    public Role create(CreateRoleRequest request) throws ApiException {
        return httpClient.post("/roles", request, Role.class);
    }

    /**
     * Update an existing role.
     */
    public Role update(String roleId, UpdateRoleRequest request) throws ApiException {
        try {
            return httpClient.put("/roles/" + roleId, request, Role.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new RoleNotFoundException(roleId);
            }
            throw e;
        }
    }

    /**
     * Delete a role.
     */
    public void delete(String roleId) throws ApiException {
        try {
            httpClient.delete("/roles/" + roleId);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new RoleNotFoundException(roleId);
            }
            throw e;
        }
    }

    // User Role Assignment

    /**
     * Get roles assigned to a user.
     */
    public UserRolesResponse getUserRoles(String userId) throws ApiException {
        return httpClient.get("/users/" + userId + "/roles", UserRolesResponse.class);
    }

    /**
     * Assign a role to a user.
     */
    public void assignRole(String userId, String roleId, String expiresAt) throws ApiException {
        Map<String, String> body = new java.util.HashMap<>();
        body.put("role_id", roleId);
        if (expiresAt != null) {
            body.put("expires_at", expiresAt);
        }
        try {
            httpClient.post("/users/" + userId + "/roles", body, Void.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new RoleNotFoundException(roleId);
            }
            throw e;
        }
    }

    /**
     * Assign a role to a user without expiration.
     */
    public void assignRole(String userId, String roleId) throws ApiException {
        assignRole(userId, roleId, null);
    }

    /**
     * Remove a role from a user.
     */
    public void removeRole(String userId, String roleId) throws ApiException {
        try {
            httpClient.delete("/users/" + userId + "/roles/" + roleId);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new RoleNotFoundException(roleId);
            }
            throw e;
        }
    }

    // Permission Checking

    /**
     * Check if a user has a specific permission.
     */
    public PermissionCheckResult checkPermission(String userId, String permission, String resourceId)
            throws ApiException {
        Map<String, String> body = new java.util.HashMap<>();
        body.put("user_id", userId);
        body.put("permission", permission);
        if (resourceId != null) {
            body.put("resource_id", resourceId);
        }
        return httpClient.post("/permissions/check", body, PermissionCheckResult.class);
    }

    /**
     * Check if a user has a specific permission (without resource context).
     */
    public PermissionCheckResult checkPermission(String userId, String permission) throws ApiException {
        return checkPermission(userId, permission, null);
    }

    // Permission Utilities

    /**
     * Check if a permission matches a pattern (supports wildcards).
     */
    public static boolean matchesPermission(String userPermission, String requiredPermission) {
        if (userPermission.equals("*") || userPermission.equals("*:*")) {
            return true;
        }

        String[] userParts = userPermission.split(":");
        String[] reqParts = requiredPermission.split(":");

        if (userParts.length != 2 || reqParts.length != 2) {
            return false;
        }

        String userResource = userParts[0];
        String userAction = userParts[1];
        String reqResource = reqParts[0];
        String reqAction = reqParts[1];

        if (userResource.equals("*")) {
            return userAction.equals("*") || userAction.equals(reqAction);
        }

        if (userResource.equals(reqResource)) {
            return userAction.equals("*") || userAction.equals(reqAction);
        }

        return false;
    }

    /**
     * Check if any user permission matches the required permission.
     */
    public static boolean hasAnyPermission(List<String> userPermissions, String requiredPermission) {
        return userPermissions.stream().anyMatch(p -> matchesPermission(p, requiredPermission));
    }

    /**
     * Check if user has all required permissions.
     */
    public static boolean hasAllPermissions(List<String> userPermissions, List<String> requiredPermissions) {
        return requiredPermissions.stream()
                .allMatch(required -> userPermissions.stream().anyMatch(p -> matchesPermission(p, required)));
    }

    /**
     * Builder for RoleClient.
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

        public RoleClient build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            return new RoleClient(this);
        }
    }
}
