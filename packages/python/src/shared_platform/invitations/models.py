"""
Invitation models.
"""

from typing import Optional, Any
from datetime import datetime
from enum import Enum
from pydantic import BaseModel, Field


class InvitationStatus(str, Enum):
    """Invitation lifecycle status."""

    PENDING = "pending"
    SENT = "sent"
    VIEWED = "viewed"
    ACCEPTED = "accepted"
    EXPIRED = "expired"
    REVOKED = "revoked"
    COMPLETED = "completed"


class InvitationType(str, Enum):
    """Type of invitation."""

    USER = "user"
    TEAM = "team"
    ORGANIZATION = "organization"
    TEST = "test"
    COURSE = "course"
    CUSTOM = "custom"


class Invitation(BaseModel):
    """Full invitation model."""

    id: Optional[str] = None
    tenant_id: str
    token: Optional[str] = None
    email: str
    name: Optional[str] = None
    invitation_type: InvitationType
    target_id: Optional[str] = None
    target_role: Optional[str] = None
    status: InvitationStatus = InvitationStatus.PENDING
    message: Optional[str] = None
    expires_at: Optional[datetime] = None
    sent_at: Optional[datetime] = None
    viewed_at: Optional[datetime] = None
    accepted_at: Optional[datetime] = None
    completed_at: Optional[datetime] = None
    metadata: Optional[dict[str, Any]] = None
    created_at: Optional[datetime] = None
    created_by: Optional[str] = None


class InvitationSummary(BaseModel):
    """Summary view of an invitation."""

    id: str
    email: str
    name: Optional[str] = None
    invitation_type: InvitationType
    status: InvitationStatus
    expires_at: Optional[datetime] = None
    sent_at: Optional[datetime] = None
    created_at: Optional[datetime] = None


class ValidatedInvitation(BaseModel):
    """Result of token validation."""

    valid: bool
    invitation: Optional[Invitation] = None
    error: Optional[str] = None


class CreateInvitationRequest(BaseModel):
    """Request to create an invitation."""

    email: str
    name: Optional[str] = None
    invitation_type: InvitationType
    target_id: Optional[str] = None
    target_role: Optional[str] = None
    message: Optional[str] = None
    expires_in_days: int = 7
    send_email: bool = True
    metadata: Optional[dict[str, Any]] = None


class BulkInvitationRequest(BaseModel):
    """Request to create multiple invitations."""

    invitations: list[CreateInvitationRequest]
    send_emails: bool = True


class BulkInvitationFailure(BaseModel):
    """Failed invitation in bulk request."""

    email: str
    reason: str


class BulkInvitationResult(BaseModel):
    """Result of bulk invitation creation."""

    successful: list[InvitationSummary] = Field(default_factory=list)
    failed: list[BulkInvitationFailure] = Field(default_factory=list)
    total: int = 0
    success_count: int = 0
    failure_count: int = 0


class AcceptInvitationRequest(BaseModel):
    """Request to accept an invitation."""

    name: Optional[str] = None
    password: Optional[str] = None
    metadata: Optional[dict[str, Any]] = None


class AcceptInvitationResponse(BaseModel):
    """Response after accepting invitation."""

    success: bool
    user_id: Optional[str] = None
    redirect_url: Optional[str] = None
    message: Optional[str] = None


class ResendInvitationRequest(BaseModel):
    """Request to resend an invitation."""

    extend_expiry: bool = True


class CleanupRequest(BaseModel):
    """Request for cleanup operation."""

    expire_pending: bool = True
    delete_older_than_days: Optional[int] = None


class CleanupResult(BaseModel):
    """Result of cleanup operation."""

    expired_count: int = 0
    deleted_count: int = 0


class Pagination(BaseModel):
    """Pagination info."""

    page: int
    page_size: int
    total_items: int
    total_pages: int
    has_next: bool = False
    has_previous: bool = False


class InvitationListResponse(BaseModel):
    """Paginated invitation list."""

    data: list[InvitationSummary] = Field(default_factory=list)
    pagination: Pagination
