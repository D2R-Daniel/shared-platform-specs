package com.platform.sdk.invitations;

import com.platform.sdk.common.ApiException;

/**
 * Base exception for invitation-related errors.
 */
public class InvitationException extends ApiException {
    public InvitationException(String message) {
        super(message);
    }

    public InvitationException(String message, int statusCode) {
        super(message, statusCode);
    }

    public InvitationException(String message, int statusCode, String errorCode) {
        super(message, statusCode, errorCode);
    }

    public InvitationException(String message, Throwable cause) {
        super(message, cause);
    }
}
