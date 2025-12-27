"""
Permission-related exceptions.
"""


class PermissionError(Exception):
    """Base permission error."""

    def __init__(self, message: str, details: str = ""):
        self.error = message
        self.details = details
        super().__init__(f"{message}: {details}" if details else message)


class RoleNotFoundError(PermissionError):
    """Role not found."""

    def __init__(self, role_id: str):
        super().__init__("role_not_found", f"Role not found: {role_id}")
        self.role_id = role_id


class RoleSlugExistsError(PermissionError):
    """Role slug already exists."""

    def __init__(self, slug: str):
        super().__init__("role_slug_exists", f"Role slug already exists: {slug}")
        self.slug = slug


class SystemRoleError(PermissionError):
    """Cannot modify or delete system role."""

    def __init__(self, role_id: str):
        super().__init__("system_role", f"Cannot modify system role: {role_id}")
        self.role_id = role_id


class RoleAlreadyAssignedError(PermissionError):
    """Role already assigned to user."""

    def __init__(self, user_id: str, role_id: str):
        super().__init__(
            "role_already_assigned",
            f"Role {role_id} already assigned to user {user_id}",
        )
        self.user_id = user_id
        self.role_id = role_id


class PermissionDeniedError(PermissionError):
    """Permission denied."""

    def __init__(self, permission: str, user_id: str = ""):
        details = f"Permission {permission} denied"
        if user_id:
            details += f" for user {user_id}"
        super().__init__("permission_denied", details)
        self.permission = permission
        self.user_id = user_id
