package com.platform.sdk.invitations;

import com.platform.sdk.common.ApiException;
import com.platform.sdk.common.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for invitation operations.
 */
public class InvitationClient {
    private final HttpClient httpClient;

    private InvitationClient(Builder builder) {
        this.httpClient = new HttpClient(builder.baseUrl, builder.timeout);
        if (builder.accessToken != null) {
            this.httpClient.setAccessToken(builder.accessToken);
        }
    }

    /**
     * Set the access token for authenticated requests.
     */
    public void setAccessToken(String accessToken) {
        this.httpClient.setAccessToken(accessToken);
    }

    // Invitation CRUD Operations

    /**
     * List invitations with optional filtering and pagination.
     */
    public InvitationListResponse list(ListInvitationsParams params) throws ApiException {
        Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
        return httpClient.get("/invitations", InvitationListResponse.class, queryParams);
    }

    /**
     * List invitations with default parameters.
     */
    public InvitationListResponse list() throws ApiException {
        return list(null);
    }

    /**
     * Get an invitation by ID.
     */
    public Invitation get(String invitationId) throws ApiException {
        try {
            return httpClient.get("/invitations/" + invitationId, Invitation.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new InvitationNotFoundException(invitationId);
            }
            throw e;
        }
    }

    /**
     * Create a new invitation.
     */
    public Invitation create(CreateInvitationRequest request) throws ApiException {
        return httpClient.post("/invitations", request, Invitation.class);
    }

    /**
     * Create multiple invitations.
     */
    public BulkInvitationResult createBulk(BulkInvitationRequest request) throws ApiException {
        return httpClient.post("/invitations/bulk", request, BulkInvitationResult.class);
    }

    /**
     * Revoke an invitation.
     */
    public void revoke(String invitationId) throws ApiException {
        try {
            httpClient.delete("/invitations/" + invitationId);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new InvitationNotFoundException(invitationId);
            }
            throw e;
        }
    }

    /**
     * Resend an invitation.
     */
    public Invitation resend(String invitationId, boolean extendExpiry) throws ApiException {
        try {
            Map<String, Object> body = Map.of("extend_expiry", extendExpiry);
            return httpClient.post("/invitations/" + invitationId + "/resend", body, Invitation.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new InvitationNotFoundException(invitationId);
            }
            throw e;
        }
    }

    /**
     * Resend an invitation with expiry extension.
     */
    public Invitation resend(String invitationId) throws ApiException {
        return resend(invitationId, true);
    }

    // Public Token Operations

    /**
     * Validate an invitation token.
     */
    public ValidatedInvitation validateToken(String token) throws ApiException {
        try {
            return httpClient.get("/invitations/validate/" + token, ValidatedInvitation.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TokenNotFoundException();
            }
            if (e.getStatusCode() == 410) {
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (errorMsg.contains("expired")) {
                    throw new TokenExpiredException();
                }
                throw new TokenRevokedException();
            }
            throw e;
        }
    }

    /**
     * Accept an invitation.
     */
    public AcceptInvitationResponse accept(String token, String name, String password,
                                           Map<String, Object> metadata) throws ApiException {
        try {
            Map<String, Object> body = new HashMap<>();
            if (name != null) body.put("name", name);
            if (password != null) body.put("password", password);
            if (metadata != null) body.put("metadata", metadata);

            return httpClient.post("/invitations/accept/" + token, body, AcceptInvitationResponse.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TokenNotFoundException();
            }
            if (e.getStatusCode() == 410) {
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (errorMsg.contains("expired")) {
                    throw new TokenExpiredException();
                }
                throw new TokenRevokedException();
            }
            throw e;
        }
    }

    /**
     * Accept an invitation with minimal data.
     */
    public AcceptInvitationResponse accept(String token) throws ApiException {
        return accept(token, null, null, null);
    }

    // Admin Operations

    /**
     * Cleanup expired invitations.
     */
    public CleanupResult cleanup(Boolean expirePending, Integer deleteOlderThanDays) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        if (expirePending != null) body.put("expire_pending", expirePending);
        if (deleteOlderThanDays != null) body.put("delete_older_than_days", deleteOlderThanDays);

        return httpClient.post("/invitations/cleanup", body, CleanupResult.class);
    }

    /**
     * Cleanup with default parameters.
     */
    public CleanupResult cleanup() throws ApiException {
        return cleanup(null, null);
    }

    /**
     * Builder for InvitationClient.
     */
    public static class Builder {
        private String baseUrl;
        private String accessToken;
        private Duration timeout = Duration.ofSeconds(30);

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public InvitationClient build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            return new InvitationClient(this);
        }
    }
}
