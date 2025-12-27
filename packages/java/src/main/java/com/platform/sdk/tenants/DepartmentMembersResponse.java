package com.platform.sdk.tenants;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Paginated list of department members.
 */
public class DepartmentMembersResponse {
    private List<DepartmentWithDetails.UserSummary> data;
    private Pagination pagination;

    public DepartmentMembersResponse() {
    }

    public List<DepartmentWithDetails.UserSummary> getData() {
        return data;
    }

    public void setData(List<DepartmentWithDetails.UserSummary> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
