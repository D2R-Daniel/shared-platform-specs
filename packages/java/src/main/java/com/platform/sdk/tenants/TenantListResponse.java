package com.platform.sdk.tenants;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Paginated list of tenants.
 */
public class TenantListResponse {
    private List<TenantSummary> data;
    private Pagination pagination;

    public TenantListResponse() {
    }

    public List<TenantSummary> getData() {
        return data;
    }

    public void setData(List<TenantSummary> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
