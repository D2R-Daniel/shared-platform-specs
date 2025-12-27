"""
Team and member models.
"""

from typing import Optional, Any
from datetime import datetime
from enum import Enum
from pydantic import BaseModel, Field


class TeamMemberRole(str, Enum):
    """Role within a team."""

    OWNER = "owner"
    ADMIN = "admin"
    MEMBER = "member"


class UserSummary(BaseModel):
    """Minimal user info."""

    id: str
    email: str
    name: Optional[str] = None
    picture: Optional[str] = None


class Team(BaseModel):
    """Team model."""

    id: Optional[str] = None
    tenant_id: str
    name: str
    slug: str
    description: Optional[str] = None
    parent_id: Optional[str] = None
    path: Optional[str] = None
    level: int = 0
    owner_id: Optional[str] = None
    avatar_url: Optional[str] = None
    is_active: bool = True
    is_private: bool = False
    settings: Optional[dict[str, Any]] = None
    metadata: Optional[dict[str, Any]] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_by: Optional[str] = None


class TeamSummary(BaseModel):
    """Summary view of a team."""

    id: str
    name: str
    slug: str
    description: Optional[str] = None
    level: int = 0
    is_active: bool = True
    is_private: bool = False
    member_count: Optional[int] = None


class TeamWithDetails(Team):
    """Team with additional details."""

    owner: Optional[UserSummary] = None
    parent: Optional[TeamSummary] = None
    children_count: Optional[int] = None
    member_count: Optional[int] = None


class TeamTree(Team):
    """Team with children for tree representation."""

    children: list["TeamTree"] = Field(default_factory=list)
    member_count: Optional[int] = None
    total_member_count: Optional[int] = None


class TeamMember(BaseModel):
    """Team member."""

    id: Optional[str] = None
    team_id: str
    user_id: str
    role: TeamMemberRole = TeamMemberRole.MEMBER
    joined_at: Optional[datetime] = None
    invited_by: Optional[str] = None
    user: Optional[UserSummary] = None


class CreateTeamRequest(BaseModel):
    """Request to create a team."""

    name: str
    slug: str
    description: Optional[str] = None
    parent_id: Optional[str] = None
    owner_id: Optional[str] = None
    avatar_url: Optional[str] = None
    is_private: bool = False
    settings: Optional[dict[str, Any]] = None
    metadata: Optional[dict[str, Any]] = None


class UpdateTeamRequest(BaseModel):
    """Request to update a team."""

    name: Optional[str] = None
    description: Optional[str] = None
    owner_id: Optional[str] = None
    avatar_url: Optional[str] = None
    is_active: Optional[bool] = None
    is_private: Optional[bool] = None
    settings: Optional[dict[str, Any]] = None
    metadata: Optional[dict[str, Any]] = None


class AddTeamMemberRequest(BaseModel):
    """Request to add a team member."""

    user_id: str
    role: TeamMemberRole = TeamMemberRole.MEMBER


class UpdateTeamMemberRequest(BaseModel):
    """Request to update a team member."""

    role: TeamMemberRole


class Pagination(BaseModel):
    """Pagination info."""

    page: int
    page_size: int
    total_items: int
    total_pages: int
    has_next: bool = False
    has_previous: bool = False


class TeamListResponse(BaseModel):
    """Paginated team list."""

    data: list[TeamSummary] = Field(default_factory=list)
    pagination: Pagination


class TeamMembersResponse(BaseModel):
    """Paginated team members list."""

    data: list[TeamMember] = Field(default_factory=list)
    pagination: Pagination


# Enable forward references
TeamTree.model_rebuild()
