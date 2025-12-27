"""
HTTP client for role and permission operations.
"""
from __future__ import annotations

from typing import Optional
import httpx
from .models import (
    Role,
    RoleSummary,
    RoleAssignment,
    RoleListResponse,
    RoleAssignmentListResponse,
    CreateRoleRequest,
    UpdateRoleRequest,
    AssignRoleRequest,
    PermissionCheckRequest,
    PermissionCheckResponse,
    UserPermissions,
)
from .exceptions import RoleNotFoundError, RoleSlugExistsError, RoleAlreadyAssignedError


class RoleClient:
    """Client for role management operations."""

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

    # Role CRUD Operations

    def list(
        self,
        page: int = 1,
        page_size: int = 20,
        is_active: Optional[bool] = None,
        is_system: Optional[bool] = None,
        search: Optional[str] = None,
        sort: str = "hierarchy_level:asc",
    ) -> RoleListResponse:
        """List roles with optional filtering."""
        params = {
            "page": page,
            "page_size": page_size,
            "sort": sort,
        }
        if is_active is not None:
            params["is_active"] = is_active
        if is_system is not None:
            params["is_system"] = is_system
        if search:
            params["search"] = search

        response = self._get_client().get("/roles", params=params)
        response.raise_for_status()
        return RoleListResponse(**response.json())

    def get(self, role_id: str) -> Role:
        """Get a role by ID."""
        response = self._get_client().get(f"/roles/{role_id}")
        if response.status_code == 404:
            raise RoleNotFoundError(role_id)
        response.raise_for_status()
        return Role(**response.json())

    def create(self, request: CreateRoleRequest) -> Role:
        """Create a new role."""
        response = self._get_client().post(
            "/roles",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 409:
            raise RoleSlugExistsError(request.slug)
        response.raise_for_status()
        return Role(**response.json())

    def update(self, role_id: str, request: UpdateRoleRequest) -> Role:
        """Update an existing role."""
        response = self._get_client().put(
            f"/roles/{role_id}",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 404:
            raise RoleNotFoundError(role_id)
        response.raise_for_status()
        return Role(**response.json())

    def delete(self, role_id: str) -> None:
        """Delete a role."""
        response = self._get_client().delete(f"/roles/{role_id}")
        if response.status_code == 404:
            raise RoleNotFoundError(role_id)
        response.raise_for_status()

    # User Role Operations

    def get_user_roles(self, user_id: str) -> RoleAssignmentListResponse:
        """Get roles assigned to a user."""
        response = self._get_client().get(f"/users/{user_id}/roles")
        response.raise_for_status()
        return RoleAssignmentListResponse(**response.json())

    def assign_role(self, user_id: str, request: AssignRoleRequest) -> RoleAssignment:
        """Assign a role to a user."""
        response = self._get_client().post(
            f"/users/{user_id}/roles",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 409:
            raise RoleAlreadyAssignedError(user_id, request.role_id)
        response.raise_for_status()
        return RoleAssignment(**response.json())

    def remove_role(self, user_id: str, role_id: str) -> None:
        """Remove a role from a user."""
        response = self._get_client().delete(f"/users/{user_id}/roles/{role_id}")
        response.raise_for_status()

    # Permission Operations

    def check_permission(self, request: PermissionCheckRequest) -> PermissionCheckResponse:
        """Check if a user has a specific permission."""
        response = self._get_client().post(
            "/permissions/check",
            json=request.model_dump(exclude_none=True),
        )
        response.raise_for_status()
        return PermissionCheckResponse(**response.json())

    def get_user_permissions(self, user_id: str) -> UserPermissions:
        """Get all effective permissions for a user."""
        response = self._get_client().get(f"/users/{user_id}/permissions")
        response.raise_for_status()
        return UserPermissions(**response.json())

    def has_permission(self, user_id: str, permission: str) -> bool:
        """Check if a user has a permission (convenience method)."""
        result = self.check_permission(
            PermissionCheckRequest(user_id=user_id, permission=permission)
        )
        return result.allowed


def matches_permission(user_permission: str, required: str) -> bool:
    """
    Check if a user permission matches the required permission.
    Supports wildcards: "users:*" matches "users:read", "*:*" matches everything.
    """
    user_parts = user_permission.split(":")
    required_parts = required.split(":")

    if len(user_parts) != 2 or len(required_parts) != 2:
        return False

    user_resource, user_action = user_parts
    req_resource, req_action = required_parts

    # Check resource match
    if user_resource != "*" and user_resource != req_resource:
        return False

    # Check action match
    if user_action != "*" and user_action != req_action:
        return False

    return True


def has_any_permission(user_permissions: list[str], required: list[str]) -> bool:
    """Check if user has any of the required permissions."""
    for req in required:
        for perm in user_permissions:
            if matches_permission(perm, req):
                return True
    return False


def has_all_permissions(user_permissions: list[str], required: list[str]) -> bool:
    """Check if user has all of the required permissions."""
    for req in required:
        found = False
        for perm in user_permissions:
            if matches_permission(perm, req):
                found = True
                break
        if not found:
            return False
    return True
