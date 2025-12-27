"""
HTTP client for invitation operations.
"""
from __future__ import annotations

from typing import Optional
import httpx
from .models import (
    Invitation,
    InvitationSummary,
    InvitationListResponse,
    InvitationStatus,
    InvitationType,
    ValidatedInvitation,
    CreateInvitationRequest,
    BulkInvitationRequest,
    BulkInvitationResult,
    AcceptInvitationRequest,
    AcceptInvitationResponse,
    ResendInvitationRequest,
    CleanupRequest,
    CleanupResult,
)
from .exceptions import (
    InvitationNotFoundError,
    TokenNotFoundError,
    TokenExpiredError,
    TokenRevokedError,
    ActiveInvitationExistsError,
)


class InvitationClient:
    """Client for invitation management operations."""

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

    # Invitation CRUD Operations

    def list(
        self,
        page: int = 1,
        page_size: int = 20,
        status: Optional[InvitationStatus] = None,
        invitation_type: Optional[InvitationType] = None,
        target_id: Optional[str] = None,
        email: Optional[str] = None,
        search: Optional[str] = None,
        sort: str = "created_at:desc",
    ) -> InvitationListResponse:
        """List invitations with optional filtering."""
        params = {
            "page": page,
            "page_size": page_size,
            "sort": sort,
        }
        if status:
            params["status"] = status.value
        if invitation_type:
            params["invitation_type"] = invitation_type.value
        if target_id:
            params["target_id"] = target_id
        if email:
            params["email"] = email
        if search:
            params["search"] = search

        response = self._get_client().get("/invitations", params=params)
        response.raise_for_status()
        return InvitationListResponse(**response.json())

    def get(self, invitation_id: str) -> Invitation:
        """Get an invitation by ID."""
        response = self._get_client().get(f"/invitations/{invitation_id}")
        if response.status_code == 404:
            raise InvitationNotFoundError(invitation_id)
        response.raise_for_status()
        return Invitation(**response.json())

    def create(self, request: CreateInvitationRequest) -> Invitation:
        """Create a new invitation."""
        response = self._get_client().post(
            "/invitations",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 409:
            raise ActiveInvitationExistsError(request.email)
        response.raise_for_status()
        return Invitation(**response.json())

    def create_bulk(self, request: BulkInvitationRequest) -> BulkInvitationResult:
        """Create multiple invitations."""
        response = self._get_client().post(
            "/invitations/bulk",
            json=request.model_dump(exclude_none=True),
        )
        response.raise_for_status()
        return BulkInvitationResult(**response.json())

    def revoke(self, invitation_id: str) -> None:
        """Revoke an invitation."""
        response = self._get_client().delete(f"/invitations/{invitation_id}")
        if response.status_code == 404:
            raise InvitationNotFoundError(invitation_id)
        response.raise_for_status()

    def resend(
        self,
        invitation_id: str,
        extend_expiry: bool = True,
    ) -> Invitation:
        """Resend an invitation."""
        response = self._get_client().post(
            f"/invitations/{invitation_id}/resend",
            json={"extend_expiry": extend_expiry},
        )
        if response.status_code == 404:
            raise InvitationNotFoundError(invitation_id)
        response.raise_for_status()
        return Invitation(**response.json())

    # Public Token Operations

    def validate_token(self, token: str) -> ValidatedInvitation:
        """Validate an invitation token (public endpoint)."""
        response = self._get_client().get(f"/invitations/validate/{token}")
        if response.status_code == 404:
            raise TokenNotFoundError(token)
        if response.status_code == 410:
            data = response.json()
            error = data.get("error", "")
            if "expired" in error.lower():
                raise TokenExpiredError(token)
            raise TokenRevokedError(token)
        response.raise_for_status()
        return ValidatedInvitation(**response.json())

    def accept(
        self,
        token: str,
        request: Optional[AcceptInvitationRequest] = None,
    ) -> AcceptInvitationResponse:
        """Accept an invitation (public endpoint)."""
        body = request.model_dump(exclude_none=True) if request else {}
        response = self._get_client().post(
            f"/invitations/accept/{token}",
            json=body,
        )
        if response.status_code == 404:
            raise TokenNotFoundError(token)
        if response.status_code == 410:
            data = response.json()
            error = data.get("error", "")
            if "expired" in error.lower():
                raise TokenExpiredError(token)
            raise TokenRevokedError(token)
        response.raise_for_status()
        return AcceptInvitationResponse(**response.json())

    # Admin Operations

    def cleanup(self, request: Optional[CleanupRequest] = None) -> CleanupResult:
        """Cleanup expired invitations (admin endpoint)."""
        body = request.model_dump(exclude_none=True) if request else {}
        response = self._get_client().post("/invitations/cleanup", json=body)
        response.raise_for_status()
        return CleanupResult(**response.json())
