package com.platform.sdk.tenants;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Paginated list of departments.
 */
public class DepartmentListResponse {
    private List<DepartmentSummary> data;
    private Pagination pagination;

    public DepartmentListResponse() {
    }

    public List<DepartmentSummary> getData() {
        return data;
    }

    public void setData(List<DepartmentSummary> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
