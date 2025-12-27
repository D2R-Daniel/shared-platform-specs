package com.platform.sdk.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Roles Tests")
class RolesTest {

    @Test
    @DisplayName("super_admin should have wildcard permission")
    void superAdminHasWildcard() {
        Set<String> permissions = Roles.getRolePermissions("super_admin");
        assertTrue(permissions.contains("*"));
    }

    @Test
    @DisplayName("admin should have users:* and settings:* permissions")
    void adminHasExpectedPermissions() {
        Set<String> permissions = Roles.getRolePermissions("admin");

        assertTrue(permissions.contains("users:*"));
        assertTrue(permissions.contains("settings:*"));
        assertTrue(permissions.contains("audit:read"));
    }

    @Test
    @DisplayName("admin should inherit manager permissions")
    void adminInheritsManagerPermissions() {
        Set<String> permissions = Roles.getRolePermissions("admin");

        // From manager
        assertTrue(permissions.contains("team:*"));
        assertTrue(permissions.contains("reports:read"));
    }

    @Test
    @DisplayName("admin should inherit user permissions")
    void adminInheritsUserPermissions() {
        Set<String> permissions = Roles.getRolePermissions("admin");

        // From user
        assertTrue(permissions.contains("profile:*"));
        assertTrue(permissions.contains("notifications:*"));
    }

    @Test
    @DisplayName("user should have profile and notifications permissions")
    void userHasExpectedPermissions() {
        Set<String> permissions = Roles.getRolePermissions("user");

        assertTrue(permissions.contains("profile:*"));
        assertTrue(permissions.contains("notifications:*"));
        assertFalse(permissions.contains("users:*"));
    }

    @Test
    @DisplayName("guest should have minimal permissions")
    void guestHasMinimalPermissions() {
        Set<String> permissions = Roles.getRolePermissions("guest");

        assertTrue(permissions.contains("profile:read"));
        assertTrue(permissions.contains("resources:read"));
        assertFalse(permissions.contains("profile:*"));
    }

    @Test
    @DisplayName("unknown role should return empty set")
    void unknownRoleReturnsEmptySet() {
        Set<String> permissions = Roles.getRolePermissions("unknown_role");
        assertTrue(permissions.isEmpty());
    }

    @Test
    @DisplayName("checkPermission should match exact permission")
    void checkPermissionExactMatch() {
        Set<String> granted = Set.of("users:read", "users:write");

        assertTrue(Roles.checkPermission(granted, "users:read"));
        assertTrue(Roles.checkPermission(granted, "users:write"));
        assertFalse(Roles.checkPermission(granted, "users:delete"));
    }

    @Test
    @DisplayName("checkPermission should match wildcard permission")
    void checkPermissionWildcard() {
        Set<String> granted = Set.of("users:*", "reports:read");

        assertTrue(Roles.checkPermission(granted, "users:read"));
        assertTrue(Roles.checkPermission(granted, "users:write"));
        assertTrue(Roles.checkPermission(granted, "users:delete"));
        assertTrue(Roles.checkPermission(granted, "reports:read"));
        assertFalse(Roles.checkPermission(granted, "reports:write"));
    }

    @Test
    @DisplayName("checkPermission should match super wildcard")
    void checkPermissionSuperWildcard() {
        Set<String> granted = Set.of("*");

        assertTrue(Roles.checkPermission(granted, "users:read"));
        assertTrue(Roles.checkPermission(granted, "settings:write"));
        assertTrue(Roles.checkPermission(granted, "anything:anything"));
    }

    @Test
    @DisplayName("getEffectivePermissions should combine role and explicit permissions")
    void getEffectivePermissionsCombinesRoleAndExplicit() {
        List<String> roles = List.of("user");
        List<String> explicit = List.of("custom:read", "custom:write");

        Set<String> permissions = Roles.getEffectivePermissions(roles, explicit);

        // From user role
        assertTrue(permissions.contains("profile:*"));
        // Explicit
        assertTrue(permissions.contains("custom:read"));
        assertTrue(permissions.contains("custom:write"));
    }

    @Test
    @DisplayName("roleInheritsFrom should correctly identify inheritance")
    void roleInheritsFromTest() {
        assertTrue(Roles.roleInheritsFrom("super_admin", "admin"));
        assertTrue(Roles.roleInheritsFrom("super_admin", "user"));
        assertTrue(Roles.roleInheritsFrom("admin", "user"));
        assertTrue(Roles.roleInheritsFrom("user", "guest"));

        assertFalse(Roles.roleInheritsFrom("user", "admin"));
        assertFalse(Roles.roleInheritsFrom("guest", "user"));
    }
}
