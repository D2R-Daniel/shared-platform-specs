package com.platform.sdk.teams;

/**
 * Exception thrown when a team member is not found.
 */
public class TeamMemberNotFoundException extends TeamException {
    private final String teamId;
    private final String userId;

    public TeamMemberNotFoundException(String teamId, String userId) {
        super("User is not a member of this team", 404, "member_not_found");
        this.teamId = teamId;
        this.userId = userId;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getUserId() {
        return userId;
    }
}
