"""Audit logging models."""

from datetime import datetime
from enum import Enum
from typing import Any, Optional

from pydantic import BaseModel, Field


class AuditEventType(str, Enum):
    """Types of audit events."""

    # Authentication events
    LOGIN_SUCCESS = "auth.login.success"
    LOGIN_FAILURE = "auth.login.failure"
    LOGOUT = "auth.logout"
    PASSWORD_CHANGE = "auth.password.change"
    PASSWORD_RESET = "auth.password.reset"
    MFA_ENABLED = "auth.mfa.enabled"
    MFA_DISABLED = "auth.mfa.disabled"

    # User management events
    USER_CREATED = "user.created"
    USER_UPDATED = "user.updated"
    USER_DELETED = "user.deleted"
    USER_SUSPENDED = "user.suspended"
    USER_ACTIVATED = "user.activated"
    ROLE_ASSIGNED = "user.role.assigned"
    ROLE_REMOVED = "user.role.removed"

    # Resource events
    RESOURCE_CREATED = "resource.created"
    RESOURCE_UPDATED = "resource.updated"
    RESOURCE_DELETED = "resource.deleted"
    RESOURCE_ACCESSED = "resource.accessed"

    # Settings events
    SETTINGS_UPDATED = "settings.updated"

    # System events
    SYSTEM_ERROR = "system.error"
    SYSTEM_WARNING = "system.warning"

    # Custom event
    CUSTOM = "custom"


class AuditEvent(BaseModel):
    """Audit event to be logged."""

    event_type: AuditEventType
    action: str
    resource_type: Optional[str] = None
    resource_id: Optional[str] = None
    description: Optional[str] = None
    metadata: Optional[dict[str, Any]] = None
    ip_address: Optional[str] = None
    user_agent: Optional[str] = None


class AuditLogEntry(BaseModel):
    """Audit log entry returned from the API."""

    id: str
    event_type: str
    action: str
    actor_id: Optional[str] = None
    actor_email: Optional[str] = None
    actor_name: Optional[str] = None
    resource_type: Optional[str] = None
    resource_id: Optional[str] = None
    description: Optional[str] = None
    metadata: Optional[dict[str, Any]] = None
    ip_address: Optional[str] = None
    user_agent: Optional[str] = None
    tenant_id: Optional[str] = None
    timestamp: datetime
    created_at: datetime


class Pagination(BaseModel):
    """Pagination metadata."""

    page: int
    page_size: int = Field(alias="pageSize")
    total_items: int = Field(alias="totalItems")
    total_pages: int = Field(alias="totalPages")
    has_next: Optional[bool] = Field(None, alias="hasNext")
    has_previous: Optional[bool] = Field(None, alias="hasPrevious")

    class Config:
        populate_by_name = True


class AuditLogListResponse(BaseModel):
    """Response containing a list of audit log entries."""

    data: list[AuditLogEntry]
    pagination: Pagination


class CreateAuditEventRequest(BaseModel):
    """Request to create an audit event."""

    event_type: AuditEventType
    action: str
    resource_type: Optional[str] = None
    resource_id: Optional[str] = None
    description: Optional[str] = None
    metadata: Optional[dict[str, Any]] = None
