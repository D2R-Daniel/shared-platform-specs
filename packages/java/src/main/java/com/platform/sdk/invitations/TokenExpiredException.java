package com.platform.sdk.invitations;

/**
 * Exception thrown when an invitation token has expired.
 */
public class TokenExpiredException extends InvitationException {
    public TokenExpiredException() {
        super("Invitation token has expired", 410, "token_expired");
    }
}
