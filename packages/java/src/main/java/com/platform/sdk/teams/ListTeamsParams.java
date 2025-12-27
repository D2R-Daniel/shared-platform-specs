package com.platform.sdk.teams;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters for listing teams.
 */
public class ListTeamsParams {
    private Integer page;
    private Integer pageSize;
    private String parentId;
    private Boolean isActive;
    private String search;
    private String sort;

    public ListTeamsParams() {
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
        if (parentId != null) params.put("parent_id", parentId);
        if (isActive != null) params.put("is_active", isActive.toString());
        if (search != null) params.put("search", search);
        if (sort != null) params.put("sort", sort);
        return params;
    }
}
