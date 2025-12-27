package com.platform.sdk.auth;

import java.util.*;

/**
 * Role and permission definitions with hierarchy support.
 */
public final class Roles {

    private Roles() {
    }

    /**
     * Role hierarchy - each role inherits from its parent.
     */
    public static final Map<String, String> ROLE_HIERARCHY = Map.of(
            "super_admin", "admin",
            "admin", "manager",
            "manager", "user",
            "user", "guest"
    );

    /**
     * Permissions assigned directly to each role.
     */
    public static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
            "super_admin", Set.of("*"),
            "admin", Set.of(
                    "users:*",
                    "settings:*",
                    "audit:read",
                    "reports:*"
            ),
            "manager", Set.of(
                    "users:read",
                    "users:create",
                    "users:update",
                    "team:*",
                    "reports:read",
                    "reports:create"
            ),
            "user", Set.of(
                    "profile:*",
                    "notifications:*",
                    "resources:read",
                    "resources:create"
            ),
            "guest", Set.of(
                    "profile:read",
                    "resources:read"
            )
    );

    /**
     * Get all permissions for a role, including inherited permissions.
     */
    public static Set<String> getRolePermissions(String role) {
        Set<String> permissions = new HashSet<>();
        String currentRole = role;

        while (currentRole != null) {
            Set<String> rolePerms = ROLE_PERMISSIONS.get(currentRole);
            if (rolePerms != null) {
                permissions.addAll(rolePerms);
            }
            currentRole = ROLE_HIERARCHY.get(currentRole);
        }

        return permissions;
    }

    /**
     * Get all effective permissions from roles and explicit permissions.
     */
    public static Set<String> getEffectivePermissions(List<String> roles, List<String> explicitPermissions) {
        Set<String> permissions = new HashSet<>();

        // Add permissions from roles
        if (roles != null) {
            for (String role : roles) {
                permissions.addAll(getRolePermissions(role));
            }
        }

        // Add explicit permissions
        if (explicitPermissions != null) {
            permissions.addAll(explicitPermissions);
        }

        return permissions;
    }

    /**
     * Check if granted permissions include the required permission.
     * Supports wildcards:
     * - "*" matches everything
     * - "resource:*" matches all actions on resource
     * - "*.action" matches action on all resources
     */
    public static boolean checkPermission(Set<String> granted, String required) {
        if (granted.contains("*") || granted.contains(required)) {
            return true;
        }

        String[] parts = required.split(":");
        if (parts.length != 2) {
            return false;
        }

        String resource = parts[0];
        String action = parts[1];

        // Check resource:* wildcard
        if (granted.contains(resource + ":*")) {
            return true;
        }

        // Check *:action wildcard
        if (granted.contains("*:" + action)) {
            return true;
        }

        return false;
    }

    /**
     * Check if a role inherits from another role.
     */
    public static boolean roleInheritsFrom(String role, String parentRole) {
        String current = role;
        while (current != null) {
            if (current.equals(parentRole)) {
                return true;
            }
            current = ROLE_HIERARCHY.get(current);
        }
        return false;
    }
}
