package com.platform.sdk.tenants;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters for listing departments.
 */
public class ListDepartmentsParams {
    private Integer page;
    private Integer pageSize;
    private String parentId;
    private Boolean isActive;
    private String search;
    private Boolean includeChildren;
    private String sort;

    public ListDepartmentsParams() {
    }

    public ListDepartmentsParams page(Integer page) {
        this.page = page;
        return this;
    }

    public ListDepartmentsParams pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ListDepartmentsParams parentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public ListDepartmentsParams isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public ListDepartmentsParams search(String search) {
        this.search = search;
        return this;
    }

    public ListDepartmentsParams includeChildren(Boolean includeChildren) {
        this.includeChildren = includeChildren;
        return this;
    }

    public ListDepartmentsParams sort(String sort) {
        this.sort = sort;
        return this;
    }

    public Map<String, String> toQueryParams() {
        Map<String, String> params = new HashMap<>();
        if (page != null) params.put("page", page.toString());
        if (pageSize != null) params.put("page_size", pageSize.toString());
        if (parentId != null) params.put("parent_id", parentId);
        if (isActive != null) params.put("is_active", isActive.toString());
        if (search != null) params.put("search", search);
        if (includeChildren != null) params.put("include_children", includeChildren.toString());
        if (sort != null) params.put("sort", sort);
        return params;
    }
}
