"""
Permissions module for RBAC.
"""

from .client import (
    RoleClient,
    matches_permission,
    has_any_permission,
    has_all_permissions,
)
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
from .exceptions import (
    PermissionError,
    RoleNotFoundError,
    RoleSlugExistsError,
    SystemRoleError,
    RoleAlreadyAssignedError,
    PermissionDeniedError,
)

__all__ = [
    # Client
    "RoleClient",
    # Utility functions
    "matches_permission",
    "has_any_permission",
    "has_all_permissions",
    # Models
    "Role",
    "RoleSummary",
    "RoleAssignment",
    "RoleListResponse",
    "RoleAssignmentListResponse",
    "CreateRoleRequest",
    "UpdateRoleRequest",
    "AssignRoleRequest",
    "PermissionCheckRequest",
    "PermissionCheckResponse",
    "UserPermissions",
    # Exceptions
    "PermissionError",
    "RoleNotFoundError",
    "RoleSlugExistsError",
    "SystemRoleError",
    "RoleAlreadyAssignedError",
    "PermissionDeniedError",
]
