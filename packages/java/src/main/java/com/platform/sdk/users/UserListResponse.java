package com.platform.sdk.users;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Response containing a list of users with pagination.
 */
public class UserListResponse {
    private List<User> data;
    private Pagination pagination;

    public UserListResponse() {
    }

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
