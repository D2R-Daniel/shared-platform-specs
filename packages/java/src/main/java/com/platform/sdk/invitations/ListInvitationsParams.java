package com.platform.sdk.invitations;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters for listing invitations.
 */
public class ListInvitationsParams {
    private Integer page;
    private Integer pageSize;
    private InvitationStatus status;
    private InvitationType invitationType;
    private String targetId;
    private String email;
    private String search;
    private String sort;

    public ListInvitationsParams() {
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public InvitationType getInvitationType() {
        return invitationType;
    }

    public void setInvitationType(InvitationType invitationType) {
        this.invitationType = invitationType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Map<String, String> toQueryParams() {
        Map<String, String> params = new HashMap<>();
        if (page != null) params.put("page", page.toString());
        if (pageSize != null) params.put("page_size", pageSize.toString());
        if (status != null) params.put("status", status.getValue());
        if (invitationType != null) params.put("invitation_type", invitationType.getValue());
        if (targetId != null) params.put("target_id", targetId);
        if (email != null) params.put("email", email);
        if (search != null) params.put("search", search);
        if (sort != null) params.put("sort", sort);
        return params;
    }
}
