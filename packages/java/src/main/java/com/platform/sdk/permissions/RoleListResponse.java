package com.platform.sdk.permissions;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Paginated list of roles.
 */
public class RoleListResponse {
    private List<RoleSummary> data;
    private Pagination pagination;

    public RoleListResponse() {
    }

    public List<RoleSummary> getData() {
        return data;
    }

    public void setData(List<RoleSummary> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
