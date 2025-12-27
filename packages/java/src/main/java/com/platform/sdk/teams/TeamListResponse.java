package com.platform.sdk.teams;

import com.platform.sdk.common.Pagination;

import java.util.List;

/**
 * Paginated list of teams.
 */
public class TeamListResponse {
    private List<TeamSummary> data;
    private Pagination pagination;

    public TeamListResponse() {
    }

    public List<TeamSummary> getData() {
        return data;
    }

    public void setData(List<TeamSummary> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
