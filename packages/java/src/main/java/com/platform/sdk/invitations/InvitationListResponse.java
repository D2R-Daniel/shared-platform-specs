package com.platform.sdk.invitations;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Paginated list of invitations.
 */
public class InvitationListResponse {
    private List<InvitationSummary> data;
    private Pagination pagination;

    public InvitationListResponse() {
    }

    public List<InvitationSummary> getData() {
        return data;
    }

    public void setData(List<InvitationSummary> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
