package com.platform.sdk.users;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Model Tests")
class UserTest {

    @Test
    @DisplayName("User should have all properties set correctly")
    void userPropertiesSetCorrectly() {
        User user = new User();
        user.setId("user-123");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setStatus("active");
        user.setRoles(List.of("user", "admin"));
        user.setTenantId("tenant-456");
        user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        user.setUpdatedAt(Instant.parse("2024-01-02T00:00:00Z"));
        user.setLastLoginAt(Instant.parse("2024-01-03T00:00:00Z"));
        user.setMetadata(Map.of("key", "value"));

        assertEquals("user-123", user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getName());
        assertEquals("https://example.com/avatar.jpg", user.getAvatarUrl());
        assertEquals("active", user.getStatus());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("user"));
        assertTrue(user.getRoles().contains("admin"));
        assertEquals("tenant-456", user.getTenantId());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNotNull(user.getLastLoginAt());
        assertNotNull(user.getMetadata());
        assertEquals("value", user.getMetadata().get("key"));
    }

    @Test
    @DisplayName("User should handle null values")
    void userHandlesNullValues() {
        User user = new User();
        user.setId("user-123");

        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getRoles());
        assertNull(user.getMetadata());
    }
}
