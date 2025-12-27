"""
Teams module for team/group management.
"""

from .client import TeamClient
from .models import (
    Team,
    TeamSummary,
    TeamWithDetails,
    TeamTree,
    TeamMember,
    TeamMemberRole,
    UserSummary,
    TeamListResponse,
    TeamMembersResponse,
    CreateTeamRequest,
    UpdateTeamRequest,
    AddTeamMemberRequest,
    UpdateTeamMemberRequest,
)
from .exceptions import (
    TeamError,
    TeamNotFoundError,
    TeamSlugExistsError,
    TeamMemberExistsError,
    TeamMemberNotFoundError,
    TeamHasMembersError,
    TeamHasChildrenError,
    TeamCircularReferenceError,
)

__all__ = [
    # Client
    "TeamClient",
    # Models
    "Team",
    "TeamSummary",
    "TeamWithDetails",
    "TeamTree",
    "TeamMember",
    "TeamMemberRole",
    "UserSummary",
    "TeamListResponse",
    "TeamMembersResponse",
    "CreateTeamRequest",
    "UpdateTeamRequest",
    "AddTeamMemberRequest",
    "UpdateTeamMemberRequest",
    # Exceptions
    "TeamError",
    "TeamNotFoundError",
    "TeamSlugExistsError",
    "TeamMemberExistsError",
    "TeamMemberNotFoundError",
    "TeamHasMembersError",
    "TeamHasChildrenError",
    "TeamCircularReferenceError",
]
