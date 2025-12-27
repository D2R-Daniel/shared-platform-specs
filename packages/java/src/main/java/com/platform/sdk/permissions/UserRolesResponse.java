package com.platform.sdk.permissions;

import java.util.List;

/**
 * Response containing user's roles.
 */
public class UserRolesResponse {
    private List<UserRole> data;
    private String userId;

    public UserRolesResponse() {
    }

    public List<UserRole> getData() {
        return data;
    }

    public void setData(List<UserRole> data) {
        this.data = data;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
