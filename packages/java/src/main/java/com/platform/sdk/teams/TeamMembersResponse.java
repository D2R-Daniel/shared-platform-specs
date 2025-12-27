package com.platform.sdk.teams;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Paginated list of team members.
 */
public class TeamMembersResponse {
    private List<TeamMember> data;
    private Pagination pagination;

    public TeamMembersResponse() {
    }

    public List<TeamMember> getData() {
        return data;
    }

    public void setData(List<TeamMember> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
