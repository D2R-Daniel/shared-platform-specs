package com.platform.sdk.permissions;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters for listing roles.
 */
public class ListRolesParams {
    private Integer page;
    private Integer pageSize;
    private Boolean isActive;
    private Boolean isSystem;
    private String search;
    private String sort;

    public ListRolesParams() {
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Map<String, String> toQueryParams() {
        Map<String, String> params = new HashMap<>();
        if (page != null) params.put("page", page.toString());
        if (pageSize != null) params.put("page_size", pageSize.toString());
        if (isActive != null) params.put("is_active", isActive.toString());
        if (isSystem != null) params.put("is_system", isSystem.toString());
        if (search != null) params.put("search", search);
        if (sort != null) params.put("sort", sort);
        return params;
    }
}
