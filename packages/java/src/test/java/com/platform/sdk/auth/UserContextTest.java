package com.platform.sdk.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserContext Tests")
class UserContextTest {

    private UserContext context;

    @BeforeEach
    void setUp() {
        context = new UserContext();
        context.setUserId("user-123");
        context.setEmail("test@example.com");
        context.setName("Test User");
        context.setTenantId("tenant-456");
        context.setRoles(List.of("user", "admin"));
        context.setPermissions(List.of("custom:read", "custom:write"));
        context.setIssuedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        context.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
    }

    @Test
    @DisplayName("hasRole should return true for existing role")
    void hasRoleExisting() {
        assertTrue(context.hasRole("user"));
        assertTrue(context.hasRole("admin"));
    }

    @Test
    @DisplayName("hasRole should return false for non-existing role")
    void hasRoleNonExisting() {
        assertFalse(context.hasRole("super_admin"));
        assertFalse(context.hasRole("guest"));
    }

    @Test
    @DisplayName("hasPermission should check explicit permissions")
    void hasPermissionExplicit() {
        assertTrue(context.hasPermission("custom:read"));
        assertTrue(context.hasPermission("custom:write"));
    }

    @Test
    @DisplayName("hasPermission should check role-based permissions")
    void hasPermissionFromRole() {
        // From admin role
        assertTrue(context.hasPermission("users:read"));
        assertTrue(context.hasPermission("users:write"));
        assertTrue(context.hasPermission("settings:read"));
    }

    @Test
    @DisplayName("hasPermission should return false for missing permission")
    void hasPermissionMissing() {
        UserContext userOnly = new UserContext();
        userOnly.setRoles(List.of("guest"));
        userOnly.setPermissions(List.of());

        assertFalse(userOnly.hasPermission("users:write"));
        assertFalse(userOnly.hasPermission("admin:something"));
    }

    @Test
    @DisplayName("isAdmin should return true for admin role")
    void isAdminWithAdminRole() {
        assertTrue(context.isAdmin());
    }

    @Test
    @DisplayName("isAdmin should return true for super_admin role")
    void isAdminWithSuperAdminRole() {
        UserContext superAdmin = new UserContext();
        superAdmin.setRoles(List.of("super_admin"));

        assertTrue(superAdmin.isAdmin());
    }

    @Test
    @DisplayName("isAdmin should return false for regular user")
    void isAdminRegularUser() {
        UserContext regularUser = new UserContext();
        regularUser.setRoles(List.of("user"));

        assertFalse(regularUser.isAdmin());
    }

    @Test
    @DisplayName("isExpired should return false for valid token")
    void isExpiredValidToken() {
        assertFalse(context.isExpired());
    }

    @Test
    @DisplayName("isExpired should return true for expired token")
    void isExpiredExpiredToken() {
        context.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        assertTrue(context.isExpired());
    }

    @Test
    @DisplayName("isExpired should return false when expiresAt is null")
    void isExpiredNullExpiry() {
        context.setExpiresAt(null);
        assertFalse(context.isExpired());
    }

    @Test
    @DisplayName("getters and setters should work correctly")
    void gettersAndSetters() {
        assertEquals("user-123", context.getUserId());
        assertEquals("test@example.com", context.getEmail());
        assertEquals("Test User", context.getName());
        assertEquals("tenant-456", context.getTenantId());
        assertEquals(2, context.getRoles().size());
        assertEquals(2, context.getPermissions().size());
        assertNotNull(context.getIssuedAt());
        assertNotNull(context.getExpiresAt());
    }
}
