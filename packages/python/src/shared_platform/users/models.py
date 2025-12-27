"""
User data models.
"""

from typing import Optional, Any
from datetime import datetime
from enum import Enum
from pydantic import BaseModel, Field, EmailStr


class UserStatus(str, Enum):
    """User account status."""

    ACTIVE = "active"
    INACTIVE = "inactive"
    PENDING = "pending"
    SUSPENDED = "suspended"
    DELETED = "deleted"


class IdentityProvider(str, Enum):
    """Authentication provider types."""

    LOCAL = "local"
    GOOGLE = "google"
    MICROSOFT = "microsoft"
    OKTA = "okta"
    SAML = "saml"
    OIDC = "oidc"


class User(BaseModel):
    """Full user model."""

    id: str = Field(..., description="User ID (UUID)")
    email: EmailStr
    email_verified: bool = False
    name: Optional[str] = None
    given_name: Optional[str] = None
    family_name: Optional[str] = None
    picture: Optional[str] = None
    phone: Optional[str] = None
    phone_verified: bool = False
    status: UserStatus = UserStatus.PENDING
    roles: list[str] = Field(default_factory=list)

    # Organization
    tenant_id: Optional[str] = None
    department_id: Optional[str] = None
    team_id: Optional[str] = None
    team_name: Optional[str] = None
    manager_id: Optional[str] = None

    # SSO / External Identity
    identity_provider: Optional[IdentityProvider] = None
    external_id: Optional[str] = Field(None, description="ID from external IdP")
    entra_object_id: Optional[str] = Field(None, description="Microsoft Entra ID Object ID")
    entra_upn: Optional[str] = Field(None, description="Microsoft Entra ID User Principal Name")
    sso_last_sync_at: Optional[datetime] = None

    metadata: dict[str, Any] = Field(default_factory=dict)
    last_login_at: Optional[datetime] = None
    created_at: datetime
    updated_at: datetime


class UserSummary(BaseModel):
    """Minimal user for lists."""

    id: str
    email: EmailStr
    name: Optional[str] = None
    picture: Optional[str] = None
    status: UserStatus
    roles: list[str] = Field(default_factory=list)
    last_login_at: Optional[datetime] = None
    created_at: datetime


class UserProfile(BaseModel):
    """Extended user profile."""

    id: str
    email: EmailStr
    email_verified: bool = False
    name: Optional[str] = None
    given_name: Optional[str] = None
    family_name: Optional[str] = None
    picture: Optional[str] = None
    phone: Optional[str] = None
    phone_verified: bool = False
    bio: Optional[str] = None
    job_title: Optional[str] = None
    department: Optional[str] = None
    location: Optional[str] = None
    website: Optional[str] = None
    social_links: dict[str, str] = Field(default_factory=dict)
    roles: list[str] = Field(default_factory=list)
    tenant_id: Optional[str] = None
    tenant_name: Optional[str] = None
    created_at: datetime
    updated_at: datetime


class NotificationPreferences(BaseModel):
    """Notification delivery preferences."""

    email_enabled: bool = True
    push_enabled: bool = True
    sms_enabled: bool = False
    in_app_enabled: bool = True
    digest_frequency: str = "realtime"
    digest_time: Optional[str] = None
    dnd_enabled: bool = False
    dnd_start: Optional[str] = None
    dnd_end: Optional[str] = None
    categories: dict[str, dict[str, bool]] = Field(default_factory=dict)


class AccessibilityPreferences(BaseModel):
    """Accessibility settings."""

    high_contrast: bool = False
    reduce_motion: bool = False
    screen_reader_hints: bool = False
    font_size: str = "medium"
    dyslexia_font: bool = False
    focus_indicators: bool = True


class UserPreferences(BaseModel):
    """User preferences and settings."""

    locale: str = "en-US"
    timezone: str = "UTC"
    date_format: str = "iso"
    time_format: str = "12h"
    first_day_of_week: int = 0
    theme: str = "system"
    sidebar_collapsed: bool = False
    compact_mode: bool = False
    notifications: NotificationPreferences = Field(default_factory=NotificationPreferences)
    accessibility: AccessibilityPreferences = Field(default_factory=AccessibilityPreferences)
    keyboard_shortcuts_enabled: bool = True
    custom: dict[str, Any] = Field(default_factory=dict)


class CreateUserRequest(BaseModel):
    """Request to create a new user."""

    email: EmailStr
    password: Optional[str] = None
    name: Optional[str] = None
    given_name: Optional[str] = None
    family_name: Optional[str] = None
    phone: Optional[str] = None
    roles: list[str] = Field(default_factory=lambda: ["user"])
    team_id: Optional[str] = None
    metadata: dict[str, Any] = Field(default_factory=dict)
    send_invitation: bool = True


class UpdateUserRequest(BaseModel):
    """Request to update a user."""

    name: Optional[str] = None
    given_name: Optional[str] = None
    family_name: Optional[str] = None
    phone: Optional[str] = None
    team_id: Optional[str] = None
    metadata: Optional[dict[str, Any]] = None


class InviteUserRequest(BaseModel):
    """Request to invite a new user."""

    email: EmailStr
    name: Optional[str] = None
    roles: list[str] = Field(default_factory=lambda: ["user"])
    team_id: Optional[str] = None
    message: Optional[str] = None
    expires_in: int = 259200  # 3 days


class Pagination(BaseModel):
    """Pagination info."""

    page: int
    page_size: int
    total_items: int
    total_pages: int
    has_next: bool = False
    has_previous: bool = False


class UserListResponse(BaseModel):
    """Paginated user list response."""

    data: list[User]
    pagination: Pagination


class UserStats(BaseModel):
    """User statistics."""

    total_users: int = 0
    active_users: int = 0
    pending_users: int = 0
    suspended_users: int = 0
    users_by_role: dict[str, int] = Field(default_factory=dict)
    users_created_last_30_days: int = 0
    users_active_last_7_days: int = 0
