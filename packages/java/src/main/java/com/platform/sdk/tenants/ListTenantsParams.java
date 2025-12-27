package com.platform.sdk.tenants;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters for listing tenants.
 */
public class ListTenantsParams {
    private Integer page;
    private Integer pageSize;
    private TenantStatus status;
    private SubscriptionPlan plan;
    private String search;
    private String sort;

    public ListTenantsParams() {
    }

    public ListTenantsParams page(Integer page) {
        this.page = page;
        return this;
    }

    public ListTenantsParams pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ListTenantsParams status(TenantStatus status) {
        this.status = status;
        return this;
    }

    public ListTenantsParams plan(SubscriptionPlan plan) {
        this.plan = plan;
        return this;
    }

    public ListTenantsParams search(String search) {
        this.search = search;
        return this;
    }

    public ListTenantsParams sort(String sort) {
        this.sort = sort;
        return this;
    }

    public Map<String, String> toQueryParams() {
        Map<String, String> params = new HashMap<>();
        if (page != null) params.put("page", page.toString());
        if (pageSize != null) params.put("page_size", pageSize.toString());
        if (status != null) params.put("status", status.getValue());
        if (plan != null) params.put("plan", plan.getValue());
        if (search != null) params.put("search", search);
        if (sort != null) params.put("sort", sort);
        return params;
    }
}
