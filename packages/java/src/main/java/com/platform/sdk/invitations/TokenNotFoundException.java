package com.platform.sdk.invitations;

/**
 * Exception thrown when an invitation token is not found.
 */
public class TokenNotFoundException extends InvitationException {
    public TokenNotFoundException() {
        super("Invalid or unknown invitation token", 404, "token_not_found");
    }
}
