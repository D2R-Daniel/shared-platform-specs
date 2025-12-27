package com.platform.sdk.permissions;

/**
 * Exception thrown when a role is not found.
 */
public class RoleNotFoundException extends RoleException {
    private final String roleId;

    public RoleNotFoundException(String roleId) {
        super("Role not found: " + roleId, 404, "role_not_found");
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }
}
