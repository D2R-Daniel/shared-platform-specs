package com.platform.sdk.invitations;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Type of invitation.
 */
public enum InvitationType {
    USER("user"),
    TEAM("team"),
    ORGANIZATION("organization"),
    TEST("test"),
    COURSE("course"),
    CUSTOM("custom");

    private final String value;

    InvitationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static InvitationType fromValue(String value) {
        for (InvitationType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown invitation type: " + value);
    }
}
