package com.platform.sdk.notifications;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Response containing a list of notifications with pagination.
 */
public class NotificationListResponse {
    private List<Notification> data;
    private Pagination pagination;

    public NotificationListResponse() {
    }

    public List<Notification> getData() {
        return data;
    }

    public void setData(List<Notification> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
