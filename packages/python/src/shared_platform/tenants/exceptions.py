"""
Tenant-related exceptions.
"""


class TenantError(Exception):
    """Base exception for tenant operations."""

    def __init__(self, message: str, details: dict = None):
        self.message = message
        self.details = details or {}
        super().__init__(message)


class TenantNotFoundError(TenantError):
    """Raised when a tenant is not found."""

    def __init__(self, tenant_id: str):
        super().__init__(
            f"Tenant not found: {tenant_id}",
            {"tenant_id": tenant_id}
        )


class TenantSlugExistsError(TenantError):
    """Raised when a tenant slug already exists."""

    def __init__(self, slug: str):
        super().__init__(
            f"Tenant slug already exists: {slug}",
            {"slug": slug}
        )


class TenantDomainExistsError(TenantError):
    """Raised when a tenant domain already exists."""

    def __init__(self, domain: str):
        super().__init__(
            f"Tenant domain already exists: {domain}",
            {"domain": domain}
        )


class SSOConfigError(TenantError):
    """Base exception for SSO configuration errors."""
    pass


class SSOConfigNotFoundError(SSOConfigError):
    """Raised when SSO configuration is not found."""

    def __init__(self, tenant_id: str):
        super().__init__(
            f"SSO configuration not found for tenant: {tenant_id}",
            {"tenant_id": tenant_id}
        )


class SSOConnectionError(SSOConfigError):
    """Raised when SSO connection test fails."""

    def __init__(self, message: str, provider: str = None):
        super().__init__(
            message,
            {"provider": provider} if provider else {}
        )


class SSOProviderError(SSOConfigError):
    """Raised when there's an issue with the SSO provider."""

    def __init__(self, provider: str, message: str):
        super().__init__(
            f"SSO provider error ({provider}): {message}",
            {"provider": provider}
        )


class DepartmentError(TenantError):
    """Base exception for department operations."""
    pass


class DepartmentNotFoundError(DepartmentError):
    """Raised when a department is not found."""

    def __init__(self, department_id: str):
        super().__init__(
            f"Department not found: {department_id}",
            {"department_id": department_id}
        )


class DepartmentCodeExistsError(DepartmentError):
    """Raised when a department code already exists in tenant."""

    def __init__(self, code: str, tenant_id: str):
        super().__init__(
            f"Department code already exists: {code}",
            {"code": code, "tenant_id": tenant_id}
        )


class DepartmentHasMembersError(DepartmentError):
    """Raised when trying to delete a department with members."""

    def __init__(self, department_id: str, member_count: int):
        super().__init__(
            f"Cannot delete department with {member_count} members",
            {"department_id": department_id, "member_count": member_count}
        )


class DepartmentHasChildrenError(DepartmentError):
    """Raised when trying to delete a department with children."""

    def __init__(self, department_id: str, children_count: int):
        super().__init__(
            f"Cannot delete department with {children_count} child departments",
            {"department_id": department_id, "children_count": children_count}
        )


class DepartmentCircularReferenceError(DepartmentError):
    """Raised when a move would create a circular reference."""

    def __init__(self, department_id: str, new_parent_id: str):
        super().__init__(
            "Cannot move department: would create circular reference",
            {"department_id": department_id, "new_parent_id": new_parent_id}
        )
