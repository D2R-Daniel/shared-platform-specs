package com.platform.sdk.tenants;

import com.fasterxml.jackson.core.type.TypeReference;
import com.platform.sdk.common.ApiException;
import com.platform.sdk.common.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for department management operations.
 */
public class DepartmentClient {
    private final HttpClient httpClient;

    private DepartmentClient(Builder builder) {
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

    /**
     * List departments with optional filtering and pagination.
     */
    public DepartmentListResponse list(ListDepartmentsParams params) throws ApiException {
        Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
        return httpClient.get("/departments", DepartmentListResponse.class, queryParams);
    }

    /**
     * List departments with default parameters.
     */
    public DepartmentListResponse list() throws ApiException {
        return list(null);
    }

    /**
     * Get department tree.
     */
    public List<DepartmentTree> getTree(GetDepartmentTreeParams params) throws ApiException {
        Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
        return httpClient.get("/departments/tree", new TypeReference<List<DepartmentTree>>() {}, queryParams);
    }

    /**
     * Get department tree with default parameters.
     */
    public List<DepartmentTree> getTree() throws ApiException {
        return getTree(null);
    }

    /**
     * Get a department by ID.
     */
    public Department get(String departmentId) throws ApiException {
        try {
            return httpClient.get("/departments/" + departmentId, Department.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new DepartmentNotFoundException(departmentId);
            }
            throw e;
        }
    }

    /**
     * Get a department with details.
     */
    public DepartmentWithDetails getWithDetails(String departmentId, boolean includeHead, boolean includeParent) throws ApiException {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("include_head", String.valueOf(includeHead));
            params.put("include_parent", String.valueOf(includeParent));
            return httpClient.get("/departments/" + departmentId, DepartmentWithDetails.class, params);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new DepartmentNotFoundException(departmentId);
            }
            throw e;
        }
    }

    /**
     * Create a new department.
     */
    public Department create(CreateDepartmentRequest request) throws ApiException {
        return httpClient.post("/departments", request, Department.class);
    }

    /**
     * Update an existing department.
     */
    public Department update(String departmentId, UpdateDepartmentRequest request) throws ApiException {
        try {
            return httpClient.put("/departments/" + departmentId, request, Department.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new DepartmentNotFoundException(departmentId);
            }
            throw e;
        }
    }

    /**
     * Delete a department.
     */
    public void delete(String departmentId) throws ApiException {
        delete(departmentId, false);
    }

    /**
     * Delete a department with optional force flag.
     */
    public void delete(String departmentId, boolean force) throws ApiException {
        try {
            if (force) {
                // Use query param for force delete
                httpClient.delete("/departments/" + departmentId + "?force=true");
            } else {
                httpClient.delete("/departments/" + departmentId);
            }
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new DepartmentNotFoundException(departmentId);
            }
            throw e;
        }
    }

    /**
     * Get department members.
     */
    public DepartmentMembersResponse getMembers(String departmentId, ListDepartmentMembersParams params) throws ApiException {
        try {
            Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
            return httpClient.get("/departments/" + departmentId + "/members", DepartmentMembersResponse.class, queryParams);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new DepartmentNotFoundException(departmentId);
            }
            throw e;
        }
    }

    /**
     * Get department members with default parameters.
     */
    public DepartmentMembersResponse getMembers(String departmentId) throws ApiException {
        return getMembers(departmentId, null);
    }

    /**
     * Move a department to a new parent.
     */
    public Department move(String departmentId, String newParentId) throws ApiException {
        try {
            Map<String, String> body = new HashMap<>();
            if (newParentId != null) {
                body.put("new_parent_id", newParentId);
            }
            return httpClient.post("/departments/" + departmentId + "/move", body, Department.class);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                throw new DepartmentNotFoundException(departmentId);
            }
            throw e;
        }
    }

    /**
     * Builder for DepartmentClient.
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

        public DepartmentClient build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            return new DepartmentClient(this);
        }
    }

    /**
     * Parameters for getting department tree.
     */
    public static class GetDepartmentTreeParams {
        private String rootId;
        private Integer maxDepth;
        private Boolean includeMembers;

        public GetDepartmentTreeParams rootId(String rootId) {
            this.rootId = rootId;
            return this;
        }

        public GetDepartmentTreeParams maxDepth(Integer maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public GetDepartmentTreeParams includeMembers(Boolean includeMembers) {
            this.includeMembers = includeMembers;
            return this;
        }

        public Map<String, String> toQueryParams() {
            Map<String, String> params = new HashMap<>();
            if (rootId != null) params.put("root_id", rootId);
            if (maxDepth != null) params.put("max_depth", maxDepth.toString());
            if (includeMembers != null) params.put("include_members", includeMembers.toString());
            return params;
        }
    }

    /**
     * Parameters for listing department members.
     */
    public static class ListDepartmentMembersParams {
        private Integer page;
        private Integer pageSize;
        private Boolean includeSubdepartments;
        private String status;

        public ListDepartmentMembersParams page(Integer page) {
            this.page = page;
            return this;
        }

        public ListDepartmentMembersParams pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public ListDepartmentMembersParams includeSubdepartments(Boolean includeSubdepartments) {
            this.includeSubdepartments = includeSubdepartments;
            return this;
        }

        public ListDepartmentMembersParams status(String status) {
            this.status = status;
            return this;
        }

        public Map<String, String> toQueryParams() {
            Map<String, String> params = new HashMap<>();
            if (page != null) params.put("page", page.toString());
            if (pageSize != null) params.put("page_size", pageSize.toString());
            if (includeSubdepartments != null) params.put("include_subdepartments", includeSubdepartments.toString());
            if (status != null) params.put("status", status);
            return params;
        }
    }
}
