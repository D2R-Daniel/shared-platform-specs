package com.platform.sdk.invitations;

import java.util.List;

/**
 * Request to create multiple invitations.
 */
public class BulkInvitationRequest {
    private List<CreateInvitationRequest> invitations;
    private Boolean sendEmails;

    public BulkInvitationRequest() {
    }

    public List<CreateInvitationRequest> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<CreateInvitationRequest> invitations) {
        this.invitations = invitations;
    }

    public Boolean getSendEmails() {
        return sendEmails;
    }

    public void setSendEmails(Boolean sendEmails) {
        this.sendEmails = sendEmails;
    }
}
