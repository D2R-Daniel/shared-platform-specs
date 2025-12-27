package com.platform.sdk.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.platform.sdk.common.HttpClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for authentication operations.
 */
public class AuthClient {
    private final HttpClient httpClient;
    private final String issuerUrl;
    private final String clientId;
    private final String clientSecret;

    private AuthClient(Builder builder) {
        this.issuerUrl = builder.issuerUrl;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.httpClient = new HttpClient(builder.issuerUrl, builder.timeout);
    }

    /**
     * Authenticate with email and password.
     */
    public TokenResponse login(String email, String password) throws AuthException {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "password");
        body.put("email", email);
        body.put("password", password);
        if (clientId != null) {
            body.put("client_id", clientId);
        }

        try {
            return httpClient.post("/oauth/token", body, TokenResponse.class);
        } catch (Exception e) {
            throw new UnauthorizedException("Login failed: " + e.getMessage());
        }
    }

    /**
     * Refresh an access token using a refresh token.
     */
    public TokenResponse refreshToken(String refreshToken) throws AuthException {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "refresh_token");
        body.put("refresh_token", refreshToken);
        if (clientId != null) {
            body.put("client_id", clientId);
        }

        try {
            return httpClient.post("/oauth/token", body, TokenResponse.class);
        } catch (Exception e) {
            throw new AuthException("Token refresh failed: " + e.getMessage(), e);
        }
    }

    /**
     * Introspect a token to check if it's valid.
     */
    public TokenIntrospectionResponse introspect(String token) throws AuthException {
        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        if (clientId != null) {
            body.put("client_id", clientId);
        }
        if (clientSecret != null) {
            body.put("client_secret", clientSecret);
        }

        try {
            return httpClient.post("/oauth/introspect", body, TokenIntrospectionResponse.class);
        } catch (Exception e) {
            throw new AuthException("Token introspection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Logout and invalidate the access token.
     */
    public void logout(String accessToken) throws AuthException {
        httpClient.setAccessToken(accessToken);
        try {
            httpClient.postVoid("/oauth/revoke", Map.of("token", accessToken));
        } catch (Exception e) {
            throw new AuthException("Logout failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get user context from an access token (decode JWT).
     */
    public UserContext getUserContext(String accessToken) throws AuthException {
        try {
            DecodedJWT jwt = JWT.decode(accessToken);

            UserContext context = new UserContext();
            context.setUserId(jwt.getSubject());
            context.setEmail(jwt.getClaim("email").asString());
            context.setName(jwt.getClaim("name").asString());
            context.setTenantId(jwt.getClaim("tenant_id").asString());

            // Get roles
            List<String> roles = jwt.getClaim("roles").asList(String.class);
            context.setRoles(roles != null ? roles : List.of());

            // Get permissions
            List<String> permissions = jwt.getClaim("permissions").asList(String.class);
            context.setPermissions(permissions != null ? permissions : List.of());

            // Get timestamps
            if (jwt.getIssuedAt() != null) {
                context.setIssuedAt(jwt.getIssuedAt().toInstant());
            }
            if (jwt.getExpiresAt() != null) {
                context.setExpiresAt(jwt.getExpiresAt().toInstant());
            }

            // Check if expired
            if (context.isExpired()) {
                throw new TokenExpiredException();
            }

            return context;
        } catch (TokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("Failed to decode token: " + e.getMessage());
        }
    }

    /**
     * List active sessions for the current user.
     */
    public List<Session> listSessions(String accessToken) throws AuthException {
        httpClient.setAccessToken(accessToken);
        try {
            return httpClient.get("/api/sessions", new TypeReference<List<Session>>() {});
        } catch (Exception e) {
            throw new AuthException("Failed to list sessions: " + e.getMessage(), e);
        }
    }

    /**
     * Revoke a specific session.
     */
    public void revokeSession(String accessToken, String sessionId) throws AuthException {
        httpClient.setAccessToken(accessToken);
        try {
            httpClient.delete("/api/sessions/" + sessionId);
        } catch (Exception e) {
            throw new AuthException("Failed to revoke session: " + e.getMessage(), e);
        }
    }

    /**
     * Revoke all sessions except the current one.
     */
    public void revokeAllSessions(String accessToken) throws AuthException {
        httpClient.setAccessToken(accessToken);
        try {
            httpClient.postVoid("/api/sessions/revoke-all", null);
        } catch (Exception e) {
            throw new AuthException("Failed to revoke sessions: " + e.getMessage(), e);
        }
    }

    /**
     * Builder for AuthClient.
     */
    public static class Builder {
        private String issuerUrl;
        private String clientId;
        private String clientSecret;
        private Duration timeout = Duration.ofSeconds(30);

        public Builder issuerUrl(String issuerUrl) {
            this.issuerUrl = issuerUrl;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public AuthClient build() {
            if (issuerUrl == null || issuerUrl.isEmpty()) {
                throw new IllegalArgumentException("issuerUrl is required");
            }
            return new AuthClient(this);
        }
    }

    /**
     * Token introspection response.
     */
    public static class TokenIntrospectionResponse {
        private boolean active;
        private String scope;
        private String clientId;
        private String username;
        private String tokenType;
        private Long exp;
        private Long iat;
        private String sub;
        private String aud;
        private String iss;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public Long getExp() {
            return exp;
        }

        public void setExp(Long exp) {
            this.exp = exp;
        }

        public Long getIat() {
            return iat;
        }

        public void setIat(Long iat) {
            this.iat = iat;
        }

        public String getSub() {
            return sub;
        }

        public void setSub(String sub) {
            this.sub = sub;
        }

        public String getAud() {
            return aud;
        }

        public void setAud(String aud) {
            this.aud = aud;
        }

        public String getIss() {
            return iss;
        }

        public void setIss(String iss) {
            this.iss = iss;
        }
    }
}
