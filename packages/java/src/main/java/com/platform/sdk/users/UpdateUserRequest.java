package com.platform.sdk.users;

import java.util.List;
import java.util.Map;

/**
 * Request to update a user.
 */
public class UpdateUserRequest {
    private String name;
    private String avatarUrl;
    private List<String> roles;
    private Map<String, Object> metadata;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest name(String name) {
        this.name = name;
        return this;
    }

    public UpdateUserRequest avatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public UpdateUserRequest roles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public UpdateUserRequest metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
