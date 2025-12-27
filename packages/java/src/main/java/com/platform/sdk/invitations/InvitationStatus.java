package com.platform.sdk.invitations;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status of an invitation.
 */
public enum InvitationStatus {
    PENDING("pending"),
    SENT("sent"),
    VIEWED("viewed"),
    ACCEPTED("accepted"),
    EXPIRED("expired"),
    REVOKED("revoked"),
    COMPLETED("completed");

    private final String value;

    InvitationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static InvitationStatus fromValue(String value) {
        for (InvitationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown invitation status: " + value);
    }
}
