package com.platform.sdk.users;

import java.util.List;
import java.util.Map;

/**
 * Request to create a new user.
 */
public class CreateUserRequest {
    private String email;
    private String name;
    private String password;
    private List<String> roles;
    private Boolean sendInvitation;
    private Map<String, Object> metadata;

    public CreateUserRequest() {
    }

    public CreateUserRequest email(String email) {
        this.email = email;
        return this;
    }

    public CreateUserRequest name(String name) {
        this.name = name;
        return this;
    }

    public CreateUserRequest password(String password) {
        this.password = password;
        return this;
    }

    public CreateUserRequest roles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public CreateUserRequest sendInvitation(Boolean sendInvitation) {
        this.sendInvitation = sendInvitation;
        return this;
    }

    public CreateUserRequest metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Boolean getSendInvitation() {
        return sendInvitation;
    }

    public void setSendInvitation(Boolean sendInvitation) {
        this.sendInvitation = sendInvitation;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
