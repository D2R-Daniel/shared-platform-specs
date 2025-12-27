package com.platform.sdk.permissions;

/**
 * Result of a permission check.
 */
public class PermissionCheckResult {
    private Boolean allowed;
    private String matchedPermission;
    private String matchedRole;
    private String reason;

    public PermissionCheckResult() {
    }

    public Boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }

    public String getMatchedPermission() {
        return matchedPermission;
    }

    public void setMatchedPermission(String matchedPermission) {
        this.matchedPermission = matchedPermission;
    }

    public String getMatchedRole() {
        return matchedRole;
    }

    public void setMatchedRole(String matchedRole) {
        this.matchedRole = matchedRole;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
