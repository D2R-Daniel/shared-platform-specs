"""
HTTP client for team operations.
"""
from __future__ import annotations

from typing import Optional
import httpx
from .models import (
    Team,
    TeamWithDetails,
    TeamTree,
    TeamMember,
    TeamListResponse,
    TeamMembersResponse,
    CreateTeamRequest,
    UpdateTeamRequest,
    AddTeamMemberRequest,
    UpdateTeamMemberRequest,
    TeamMemberRole,
)
from .exceptions import (
    TeamNotFoundError,
    TeamSlugExistsError,
    TeamMemberExistsError,
    TeamMemberNotFoundError,
    TeamCircularReferenceError,
)


class TeamClient:
    """Client for team management operations."""

    def __init__(
        self,
        base_url: str,
        access_token: Optional[str] = None,
        timeout: float = 30.0,
    ):
        self.base_url = base_url.rstrip("/")
        self._access_token = access_token
        self._timeout = timeout
        self._client: Optional[httpx.Client] = None

    def _get_client(self) -> httpx.Client:
        if self._client is None:
            headers = {"Content-Type": "application/json"}
            if self._access_token:
                headers["Authorization"] = f"Bearer {self._access_token}"
            self._client = httpx.Client(
                base_url=self.base_url,
                timeout=self._timeout,
                headers=headers,
            )
        return self._client

    def set_access_token(self, token: str) -> None:
        """Update the access token."""
        self._access_token = token
        if self._client:
            self._client.headers["Authorization"] = f"Bearer {token}"

    def close(self) -> None:
        """Close the HTTP client."""
        if self._client:
            self._client.close()
            self._client = None

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.close()

    # Team CRUD Operations

    def list(
        self,
        page: int = 1,
        page_size: int = 20,
        parent_id: Optional[str] = None,
        owner_id: Optional[str] = None,
        is_active: Optional[bool] = None,
        is_private: Optional[bool] = None,
        search: Optional[str] = None,
        sort: str = "name:asc",
    ) -> TeamListResponse:
        """List teams with optional filtering."""
        params = {
            "page": page,
            "page_size": page_size,
            "sort": sort,
        }
        if parent_id:
            params["parent_id"] = parent_id
        if owner_id:
            params["owner_id"] = owner_id
        if is_active is not None:
            params["is_active"] = is_active
        if is_private is not None:
            params["is_private"] = is_private
        if search:
            params["search"] = search

        response = self._get_client().get("/teams", params=params)
        response.raise_for_status()
        return TeamListResponse(**response.json())

    def get_tree(
        self,
        root_id: Optional[str] = None,
        max_depth: int = 10,
        include_members: bool = False,
    ) -> list[TeamTree]:
        """Get team hierarchy tree."""
        params = {
            "max_depth": max_depth,
            "include_members": include_members,
        }
        if root_id:
            params["root_id"] = root_id

        response = self._get_client().get("/teams/tree", params=params)
        response.raise_for_status()
        data = response.json()
        return [TeamTree(**t) for t in data.get("data", data)]

    def get(
        self,
        team_id: str,
        include_owner: bool = False,
        include_parent: bool = False,
    ) -> TeamWithDetails:
        """Get a team by ID."""
        params = {
            "include_owner": include_owner,
            "include_parent": include_parent,
        }
        response = self._get_client().get(f"/teams/{team_id}", params=params)
        if response.status_code == 404:
            raise TeamNotFoundError(team_id)
        response.raise_for_status()
        return TeamWithDetails(**response.json())

    def create(self, request: CreateTeamRequest) -> Team:
        """Create a new team."""
        response = self._get_client().post(
            "/teams",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 409:
            raise TeamSlugExistsError(request.slug)
        response.raise_for_status()
        return Team(**response.json())

    def update(self, team_id: str, request: UpdateTeamRequest) -> Team:
        """Update an existing team."""
        response = self._get_client().put(
            f"/teams/{team_id}",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 404:
            raise TeamNotFoundError(team_id)
        response.raise_for_status()
        return Team(**response.json())

    def delete(self, team_id: str, force: bool = False) -> None:
        """Delete a team."""
        params = {"force": force} if force else {}
        response = self._get_client().delete(f"/teams/{team_id}", params=params)
        if response.status_code == 404:
            raise TeamNotFoundError(team_id)
        response.raise_for_status()

    def move(self, team_id: str, new_parent_id: Optional[str] = None) -> Team:
        """Move a team to a new parent."""
        response = self._get_client().post(
            f"/teams/{team_id}/move",
            json={"new_parent_id": new_parent_id},
        )
        if response.status_code == 404:
            raise TeamNotFoundError(team_id)
        if response.status_code == 409:
            raise TeamCircularReferenceError(team_id, new_parent_id or "root")
        response.raise_for_status()
        return Team(**response.json())

    # Team Member Operations

    def list_members(
        self,
        team_id: str,
        page: int = 1,
        page_size: int = 20,
        role: Optional[TeamMemberRole] = None,
        search: Optional[str] = None,
    ) -> TeamMembersResponse:
        """List team members."""
        params = {
            "page": page,
            "page_size": page_size,
        }
        if role:
            params["role"] = role.value
        if search:
            params["search"] = search

        response = self._get_client().get(f"/teams/{team_id}/members", params=params)
        if response.status_code == 404:
            raise TeamNotFoundError(team_id)
        response.raise_for_status()
        return TeamMembersResponse(**response.json())

    def add_member(self, team_id: str, request: AddTeamMemberRequest) -> TeamMember:
        """Add a member to a team."""
        response = self._get_client().post(
            f"/teams/{team_id}/members",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 404:
            raise TeamNotFoundError(team_id)
        if response.status_code == 409:
            raise TeamMemberExistsError(team_id, request.user_id)
        response.raise_for_status()
        return TeamMember(**response.json())

    def update_member(
        self,
        team_id: str,
        user_id: str,
        request: UpdateTeamMemberRequest,
    ) -> TeamMember:
        """Update a team member's role."""
        response = self._get_client().put(
            f"/teams/{team_id}/members/{user_id}",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 404:
            raise TeamMemberNotFoundError(team_id, user_id)
        response.raise_for_status()
        return TeamMember(**response.json())

    def remove_member(self, team_id: str, user_id: str) -> None:
        """Remove a member from a team."""
        response = self._get_client().delete(f"/teams/{team_id}/members/{user_id}")
        if response.status_code == 404:
            raise TeamMemberNotFoundError(team_id, user_id)
        response.raise_for_status()
