package com.platform.sdk.users;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters for listing users.
 */
public class ListUsersParams {
    private Integer page;
    private Integer pageSize;
    private String search;
    private String status;
    private String role;
    private String sortBy;
    private String sortOrder;

    public ListUsersParams() {
    }

    public ListUsersParams page(Integer page) {
        this.page = page;
        return this;
    }

    public ListUsersParams pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ListUsersParams search(String search) {
        this.search = search;
        return this;
    }

    public ListUsersParams status(String status) {
        this.status = status;
        return this;
    }

    public ListUsersParams role(String role) {
        this.role = role;
        return this;
    }

    public ListUsersParams sortBy(String sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    public ListUsersParams sortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public String getSearch() {
        return search;
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * Convert to query parameters map.
     */
    public Map<String, String> toQueryParams() {
        Map<String, String> params = new HashMap<>();
        if (page != null) params.put("page", page.toString());
        if (pageSize != null) params.put("page_size", pageSize.toString());
        if (search != null) params.put("search", search);
        if (status != null) params.put("status", status);
        if (role != null) params.put("role", role);
        if (sortBy != null) params.put("sort_by", sortBy);
        if (sortOrder != null) params.put("sort_order", sortOrder);
        return params;
    }
}
