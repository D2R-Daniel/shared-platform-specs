package com.platform.sdk.invitations;

/**
 * Exception thrown when an invitation has been revoked.
 */
public class TokenRevokedException extends InvitationException {
    public TokenRevokedException() {
        super("Invitation has been revoked", 410, "token_revoked");
    }
}
