"""
Permission and Role models for RBAC.
"""

from typing import Optional, Any
from datetime import datetime
from enum import Enum
from pydantic import BaseModel, Field


class Role(BaseModel):
    """Role model with permissions."""

    id: Optional[str] = None
    tenant_id: Optional[str] = None
    name: str
    slug: str
    description: Optional[str] = None
    permissions: list[str] = Field(default_factory=list)
    hierarchy_level: int = 50
    is_system: bool = False
    is_active: bool = True
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    created_by: Optional[str] = None
    updated_by: Optional[str] = None


class RoleSummary(BaseModel):
    """Summary view of a role."""

    id: str
    name: str
    slug: str
    hierarchy_level: int = 50
    is_system: bool = False
    is_active: bool = True
    permission_count: Optional[int] = None


class RoleAssignment(BaseModel):
    """Assignment of a role to a user."""

    id: Optional[str] = None
    user_id: str
    role_id: str
    role: Optional[RoleSummary] = None
    granted_at: Optional[datetime] = None
    granted_by: Optional[str] = None
    expires_at: Optional[datetime] = None


class CreateRoleRequest(BaseModel):
    """Request to create a new role."""

    name: str
    slug: str
    description: Optional[str] = None
    permissions: list[str] = Field(default_factory=list)
    hierarchy_level: int = 50


class UpdateRoleRequest(BaseModel):
    """Request to update a role."""

    name: Optional[str] = None
    description: Optional[str] = None
    permissions: Optional[list[str]] = None
    hierarchy_level: Optional[int] = None
    is_active: Optional[bool] = None


class AssignRoleRequest(BaseModel):
    """Request to assign a role to a user."""

    role_id: str
    expires_at: Optional[datetime] = None


class PermissionCheckRequest(BaseModel):
    """Request to check if a user has a permission."""

    user_id: str
    permission: str
    context: Optional[dict[str, Any]] = None


class PermissionCheckResponse(BaseModel):
    """Result of a permission check."""

    allowed: bool
    matched_permission: Optional[str] = None
    matched_role: Optional[str] = None


class UserPermissions(BaseModel):
    """User's effective permissions."""

    permissions: list[str] = Field(default_factory=list)
    roles: list[RoleSummary] = Field(default_factory=list)


class Pagination(BaseModel):
    """Pagination info."""

    page: int
    page_size: int
    total_items: int
    total_pages: int
    has_next: bool = False
    has_previous: bool = False


class RoleListResponse(BaseModel):
    """Paginated role list."""

    data: list[RoleSummary] = Field(default_factory=list)
    pagination: Pagination


class RoleAssignmentListResponse(BaseModel):
    """List of role assignments."""

    data: list[RoleAssignment] = Field(default_factory=list)
