package com.platform.sdk.notifications;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters for listing notifications.
 */
public class ListNotificationsParams {
    private Integer page;
    private Integer pageSize;
    private String status;
    private String category;
    private String type;

    public ListNotificationsParams() {
    }

    public ListNotificationsParams page(Integer page) {
        this.page = page;
        return this;
    }

    public ListNotificationsParams pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ListNotificationsParams status(String status) {
        this.status = status;
        return this;
    }

    public ListNotificationsParams category(String category) {
        this.category = category;
        return this;
    }

    public ListNotificationsParams type(String type) {
        this.type = type;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    /**
     * Convert to query parameters map.
     */
    public Map<String, String> toQueryParams() {
        Map<String, String> params = new HashMap<>();
        if (page != null) params.put("page", page.toString());
        if (pageSize != null) params.put("page_size", pageSize.toString());
        if (status != null) params.put("status", status);
        if (category != null) params.put("category", category);
        if (type != null) params.put("type", type);
        return params;
    }
}
