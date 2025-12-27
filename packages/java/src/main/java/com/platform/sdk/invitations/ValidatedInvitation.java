package com.platform.sdk.invitations;

/**
 * Result of token validation.
 */
public class ValidatedInvitation {
    private Boolean valid;
    private Invitation invitation;
    private String error;

    public ValidatedInvitation() {
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Invitation getInvitation() {
        return invitation;
    }

    public void setInvitation(Invitation invitation) {
        this.invitation = invitation;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
