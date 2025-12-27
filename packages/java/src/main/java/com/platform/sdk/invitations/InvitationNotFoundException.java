package com.platform.sdk.invitations;

/**
 * Exception thrown when an invitation is not found.
 */
public class InvitationNotFoundException extends InvitationException {
    private final String invitationId;

    public InvitationNotFoundException(String invitationId) {
        super("Invitation not found: " + invitationId, 404, "invitation_not_found");
        this.invitationId = invitationId;
    }

    public String getInvitationId() {
        return invitationId;
    }
}
