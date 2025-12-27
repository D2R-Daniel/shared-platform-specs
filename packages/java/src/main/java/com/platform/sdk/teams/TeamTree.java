package com.platform.sdk.teams;

import java.util.List;

/**
 * Team with hierarchical children for tree display.
 */
public class TeamTree extends Team {
    private List<TeamTree> children;
    private Integer memberCount;
    private Integer totalMemberCount;

    public TeamTree() {
        super();
    }

    public List<TeamTree> getChildren() {
        return children;
    }

    public void setChildren(List<TeamTree> children) {
        this.children = children;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getTotalMemberCount() {
        return totalMemberCount;
    }

    public void setTotalMemberCount(Integer totalMemberCount) {
        this.totalMemberCount = totalMemberCount;
    }
}
