package com.platform.sdk.users;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * User model.
 */
public class User {
    private String id;
    private String email;
    private String name;
    private String avatarUrl;
    private String status;
    private List<String> roles;
    private String tenantId;

    // Organization
    private String departmentId;
    private String managerId;

    // SSO / External Identity
    private IdentityProvider identityProvider;
    private String externalId;
    private String entraObjectId;
    private String entraUpn;
    private Instant ssoLastSyncAt;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private Map<String, Object> metadata;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getEntraObjectId() {
        return entraObjectId;
    }

    public void setEntraObjectId(String entraObjectId) {
        this.entraObjectId = entraObjectId;
    }

    public String getEntraUpn() {
        return entraUpn;
    }

    public void setEntraUpn(String entraUpn) {
        this.entraUpn = entraUpn;
    }

    public Instant getSsoLastSyncAt() {
        return ssoLastSyncAt;
    }

    public void setSsoLastSyncAt(Instant ssoLastSyncAt) {
        this.ssoLastSyncAt = ssoLastSyncAt;
    }
}
