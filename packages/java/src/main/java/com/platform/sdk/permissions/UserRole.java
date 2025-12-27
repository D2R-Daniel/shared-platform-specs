package com.platform.sdk.permissions;

import java.time.Instant;

/**
 * A role assigned to a user.
 */
public class UserRole {
    private String roleId;
    private String roleName;
    private String roleSlug;
    private Instant grantedAt;
    private Instant expiresAt;

    public UserRole() {
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleSlug() {
        return roleSlug;
    }

    public void setRoleSlug(String roleSlug) {
        this.roleSlug = roleSlug;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(Instant grantedAt) {
        this.grantedAt = grantedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
