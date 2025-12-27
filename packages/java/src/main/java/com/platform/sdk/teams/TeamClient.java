package com.platform.sdk.teams;

import com.platform.sdk.common.ApiException;
import com.platform.sdk.common.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for team management operations.
 */
public class TeamClient {
    private final HttpClient httpClient;

    private TeamClient(Builder builder) {
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

    // Team CRUD Operations

    /**
     * List teams with optional filtering and pagination.
     */
    public TeamListResponse list(ListTeamsParams params) throws ApiException {
        Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
        return httpClient.get("/teams", TeamListResponse.class, queryParams);
    }

    /**
     * List teams with default parameters.
     */
    public TeamListResponse list() throws ApiException {
        return list(null);
    }

    /**
     * Get team hierarchy tree.
     */
    public List<TeamTree> getTree(String rootId, Integer maxDepth, Boolean includeMembers) throws ApiException {
        Map<String, String> params = new HashMap<>();
        if (rootId != null) params.put("root_id", rootId);
        if (maxDepth != null) params.put("max_depth", maxDepth.toString());
        if (includeMembers != null) params.put("include_members", includeMembers.toString());

        TeamTreeResponse response = httpClient.get("/teams/tree", TeamTreeResponse.class, params);
        return response.getData();
    }

    /**
     * Get team hierarchy tree with defaults.
     */
    public List<TeamTree> getTree() throws ApiException {
        return getTree(null, null, null);
    }

    /**
     * Get a team by ID.
     */
    public Team get(String teamId) throws ApiException {
        try {
            return httpClient.get("/teams/" + teamId, Team.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TeamNotFoundException(teamId);
            }
            throw e;
        }
    }

    /**
     * Get a team by slug.
     */
    public Team getBySlug(String slug) throws ApiException {
        try {
            return httpClient.get("/teams/slug/" + slug, Team.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TeamNotFoundException(slug);
            }
            throw e;
        }
    }

    /**
     * Create a new team.
     */
    public Team create(CreateTeamRequest request) throws ApiException {
        return httpClient.post("/teams", request, Team.class);
    }

    /**
     * Update an existing team.
     */
    public Team update(String teamId, UpdateTeamRequest request) throws ApiException {
        try {
            return httpClient.put("/teams/" + teamId, request, Team.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TeamNotFoundException(teamId);
            }
            throw e;
        }
    }

    /**
     * Delete a team.
     */
    public void delete(String teamId) throws ApiException {
        delete(teamId, false);
    }

    /**
     * Delete a team with optional force flag.
     */
    public void delete(String teamId, boolean force) throws ApiException {
        try {
            String path = "/teams/" + teamId + (force ? "?force=true" : "");
            httpClient.delete(path);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TeamNotFoundException(teamId);
            }
            throw e;
        }
    }

    /**
     * Move a team to a new parent.
     */
    public Team move(String teamId, String newParentId) throws ApiException {
        try {
            Map<String, String> body = new HashMap<>();
            if (newParentId != null) {
                body.put("new_parent_id", newParentId);
            }
            return httpClient.post("/teams/" + teamId + "/move", body, Team.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TeamNotFoundException(teamId);
            }
            throw e;
        }
    }

    // Team Member Operations

    /**
     * List members of a team.
     */
    public TeamMembersResponse listMembers(String teamId, Integer page, Integer pageSize, TeamMemberRole role)
            throws ApiException {
        try {
            Map<String, String> params = new HashMap<>();
            if (page != null) params.put("page", page.toString());
            if (pageSize != null) params.put("page_size", pageSize.toString());
            if (role != null) params.put("role", role.getValue());
            params.put("include_user", "true");

            return httpClient.get("/teams/" + teamId + "/members", TeamMembersResponse.class, params);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TeamNotFoundException(teamId);
            }
            throw e;
        }
    }

    /**
     * List members of a team with defaults.
     */
    public TeamMembersResponse listMembers(String teamId) throws ApiException {
        return listMembers(teamId, null, null, null);
    }

    /**
     * Add a member to a team.
     */
    public TeamMember addMember(String teamId, String userId, TeamMemberRole role) throws ApiException {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("user_id", userId);
            if (role != null) {
                body.put("role", role.getValue());
            }
            return httpClient.post("/teams/" + teamId + "/members", body, TeamMember.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new TeamNotFoundException(teamId);
            }
            throw e;
        }
    }

    /**
     * Add a member to a team with default role.
     */
    public TeamMember addMember(String teamId, String userId) throws ApiException {
        return addMember(teamId, userId, null);
    }

    /**
     * Update a team member's role.
     */
    public TeamMember updateMember(String teamId, String userId, TeamMemberRole role) throws ApiException {
        try {
            Map<String, String> body = Map.of("role", role.getValue());
            return httpClient.put("/teams/" + teamId + "/members/" + userId, body, TeamMember.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                if (e.getErrorCode() != null && e.getErrorCode().equals("member_not_found")) {
                    throw new TeamMemberNotFoundException(teamId, userId);
                }
                throw new TeamNotFoundException(teamId);
            }
            throw e;
        }
    }

    /**
     * Remove a member from a team.
     */
    public void removeMember(String teamId, String userId) throws ApiException {
        try {
            httpClient.delete("/teams/" + teamId + "/members/" + userId);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                if (e.getErrorCode() != null && e.getErrorCode().equals("member_not_found")) {
                    throw new TeamMemberNotFoundException(teamId, userId);
                }
                throw new TeamNotFoundException(teamId);
            }
            throw e;
        }
    }

    /**
     * Builder for TeamClient.
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

        public TeamClient build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            return new TeamClient(this);
        }
    }

    /**
     * Internal response wrapper for team tree.
     */
    private static class TeamTreeResponse {
        private List<TeamTree> data;

        public List<TeamTree> getData() {
            return data;
        }

        public void setData(List<TeamTree> data) {
            this.data = data;
        }
    }
}
