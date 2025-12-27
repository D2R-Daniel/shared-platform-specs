package com.platform.sdk.auth;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authenticated user context extracted from JWT token.
 */
public class UserContext {
    private String userId;
    private String email;
    private String name;
    private String tenantId;
    private List<String> roles;
    private List<String> permissions;
    private Instant issuedAt;
    private Instant expiresAt;
    private Map<String, Object> metadata;

    public UserContext() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Check if user has the specified role.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Check if user has the specified permission.
     * Supports wildcard matching (e.g., "users:*" matches "users:read").
     */
    public boolean hasPermission(String permission) {
        if (permissions == null) {
            return false;
        }

        // Get all permissions including from roles
        Set<String> allPermissions = Roles.getEffectivePermissions(roles, permissions);
        return Roles.checkPermission(allPermissions, permission);
    }

    /**
     * Check if user is an admin (has admin or super_admin role).
     */
    public boolean isAdmin() {
        return hasRole("admin") || hasRole("super_admin");
    }

    /**
     * Check if token has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
