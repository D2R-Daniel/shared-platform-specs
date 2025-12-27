"""
Team-related exceptions.
"""


class TeamError(Exception):
    """Base team error."""

    def __init__(self, message: str, details: str = ""):
        self.error = message
        self.details = details
        super().__init__(f"{message}: {details}" if details else message)


class TeamNotFoundError(TeamError):
    """Team not found."""

    def __init__(self, team_id: str):
        super().__init__("team_not_found", f"Team not found: {team_id}")
        self.team_id = team_id


class TeamSlugExistsError(TeamError):
    """Team slug already exists."""

    def __init__(self, slug: str):
        super().__init__("team_slug_exists", f"Team slug already exists: {slug}")
        self.slug = slug


class TeamMemberExistsError(TeamError):
    """User is already a team member."""

    def __init__(self, team_id: str, user_id: str):
        super().__init__(
            "member_exists",
            f"User {user_id} is already a member of team {team_id}",
        )
        self.team_id = team_id
        self.user_id = user_id


class TeamMemberNotFoundError(TeamError):
    """Team member not found."""

    def __init__(self, team_id: str, user_id: str):
        super().__init__(
            "member_not_found",
            f"User {user_id} is not a member of team {team_id}",
        )
        self.team_id = team_id
        self.user_id = user_id


class TeamHasMembersError(TeamError):
    """Cannot delete team with members."""

    def __init__(self, team_id: str, member_count: int):
        super().__init__(
            "team_has_members",
            f"Cannot delete team {team_id} with {member_count} members",
        )
        self.team_id = team_id
        self.member_count = member_count


class TeamHasChildrenError(TeamError):
    """Cannot delete team with child teams."""

    def __init__(self, team_id: str, children_count: int):
        super().__init__(
            "team_has_children",
            f"Cannot delete team {team_id} with {children_count} child teams",
        )
        self.team_id = team_id
        self.children_count = children_count


class TeamCircularReferenceError(TeamError):
    """Moving team would create circular reference."""

    def __init__(self, team_id: str, new_parent_id: str):
        super().__init__(
            "circular_reference",
            f"Moving team {team_id} to {new_parent_id} would create circular reference",
        )
        self.team_id = team_id
        self.new_parent_id = new_parent_id
