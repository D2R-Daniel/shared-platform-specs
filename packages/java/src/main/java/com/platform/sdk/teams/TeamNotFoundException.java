package com.platform.sdk.teams;

/**
 * Exception thrown when a team is not found.
 */
public class TeamNotFoundException extends TeamException {
    private final String teamId;

    public TeamNotFoundException(String teamId) {
        super("Team not found: " + teamId, 404, "team_not_found");
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }
}
