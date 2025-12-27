package com.platform.sdk.invitations;

/**
 * Response after accepting an invitation.
 */
public class AcceptInvitationResponse {
    private Boolean success;
    private String userId;
    private String redirectUrl;
    private String message;

    public AcceptInvitationResponse() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
