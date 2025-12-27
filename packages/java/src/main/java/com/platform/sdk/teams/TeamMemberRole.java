package com.platform.sdk.teams;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Role of a team member.
 */
public enum TeamMemberRole {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member");

    private final String value;

    TeamMemberRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TeamMemberRole fromValue(String value) {
        for (TeamMemberRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown team member role: " + value);
    }
}
