package com.platform.sdk.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthClient Tests")
class AuthClientTest {

    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        authClient = new AuthClient.Builder()
                .issuerUrl("https://auth.example.com")
                .clientId("test-client")
                .build();
    }

    @Test
    @DisplayName("Builder should require issuerUrl")
    void builderRequiresIssuerUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                new AuthClient.Builder().build()
        );
    }

    @Test
    @DisplayName("Builder should create client with all options")
    void builderCreatesClientWithOptions() {
        AuthClient client = new AuthClient.Builder()
                .issuerUrl("https://auth.example.com")
                .clientId("my-client")
                .clientSecret("my-secret")
                .build();

        assertNotNull(client);
    }

    @Test
    @DisplayName("getUserContext should decode valid JWT token")
    void getUserContextDecodesValidToken() throws AuthException {
        String token = createTestToken(
                "user-123",
                "test@example.com",
                "Test User",
                "tenant-456",
                List.of("user", "admin"),
                List.of("custom:read"),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        UserContext context = authClient.getUserContext(token);

        assertEquals("user-123", context.getUserId());
        assertEquals("test@example.com", context.getEmail());
        assertEquals("Test User", context.getName());
        assertEquals("tenant-456", context.getTenantId());
        assertTrue(context.getRoles().contains("user"));
        assertTrue(context.getRoles().contains("admin"));
        assertTrue(context.getPermissions().contains("custom:read"));
    }

    @Test
    @DisplayName("getUserContext should throw TokenExpiredException for expired token")
    void getUserContextThrowsForExpiredToken() {
        String token = createTestToken(
                "user-123",
                "test@example.com",
                "Test User",
                "tenant-456",
                List.of("user"),
                List.of(),
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        assertThrows(TokenExpiredException.class, () ->
                authClient.getUserContext(token)
        );
    }

    @Test
    @DisplayName("getUserContext should throw InvalidTokenException for invalid token")
    void getUserContextThrowsForInvalidToken() {
        assertThrows(InvalidTokenException.class, () ->
                authClient.getUserContext("invalid-token")
        );
    }

    @Test
    @DisplayName("getUserContext should handle token with missing optional claims")
    void getUserContextHandlesMissingClaims() throws AuthException {
        // Create a minimal token with only required claims
        Algorithm algorithm = Algorithm.HMAC256("test-secret");
        String token = JWT.create()
                .withSubject("user-123")
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(algorithm);

        UserContext context = authClient.getUserContext(token);

        assertEquals("user-123", context.getUserId());
        assertNull(context.getEmail());
        assertNull(context.getName());
        assertNotNull(context.getRoles()); // Should be empty list, not null
        assertNotNull(context.getPermissions()); // Should be empty list, not null
    }

    @Test
    @DisplayName("getUserContext should correctly identify admin user")
    void getUserContextIdentifiesAdmin() throws AuthException {
        String token = createTestToken(
                "admin-123",
                "admin@example.com",
                "Admin User",
                "tenant-456",
                List.of("admin"),
                List.of(),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        UserContext context = authClient.getUserContext(token);

        assertTrue(context.isAdmin());
        assertTrue(context.hasRole("admin"));
    }

    /**
     * Helper method to create a test JWT token.
     */
    private String createTestToken(
            String userId,
            String email,
            String name,
            String tenantId,
            List<String> roles,
            List<String> permissions,
            Instant expiresAt
    ) {
        Algorithm algorithm = Algorithm.HMAC256("test-secret");

        return JWT.create()
                .withSubject(userId)
                .withClaim("email", email)
                .withClaim("name", name)
                .withClaim("tenant_id", tenantId)
                .withClaim("roles", roles)
                .withClaim("permissions", permissions)
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(expiresAt))
                .sign(algorithm);
    }
}
