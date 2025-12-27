"""Audit logging client for tracking user actions and system events."""

from __future__ import annotations

from datetime import datetime
from typing import Optional

import httpx

from .models import (
    AuditEvent,
    AuditLogEntry,
    AuditLogListResponse,
    CreateAuditEventRequest,
)


class AuditClient:
    """Client for audit logging operations."""

    def __init__(
        self,
        base_url: str,
        access_token: Optional[str] = None,
        timeout: float = 30.0,
    ):
        """
        Initialize the audit client.

        Args:
            base_url: Base URL of the API server
            access_token: Optional access token for authentication
            timeout: Request timeout in seconds
        """
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self._access_token = access_token

    def set_access_token(self, token: str) -> None:
        """Set the access token for authenticated requests."""
        self._access_token = token

    def _get_headers(self) -> dict[str, str]:
        """Get request headers including auth token."""
        headers = {"Content-Type": "application/json"}
        if self._access_token:
            headers["Authorization"] = f"Bearer {self._access_token}"
        return headers

    async def log(
        self,
        event: AuditEvent,
        ip_address: Optional[str] = None,
        user_agent: Optional[str] = None,
    ) -> AuditLogEntry:
        """
        Log an audit event.

        Args:
            event: The audit event to log
            ip_address: Optional IP address of the request
            user_agent: Optional user agent of the request

        Returns:
            The created audit log entry
        """
        data = event.model_dump(exclude_none=True)
        if ip_address:
            data["ip_address"] = ip_address
        if user_agent:
            data["user_agent"] = user_agent

        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.post(
                f"{self.base_url}/api/audit",
                json=data,
                headers=self._get_headers(),
            )
            response.raise_for_status()
            return AuditLogEntry(**response.json())

    async def list(
        self,
        page: int = 1,
        page_size: int = 20,
        event_type: Optional[str] = None,
        actor_id: Optional[str] = None,
        resource_type: Optional[str] = None,
        resource_id: Optional[str] = None,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None,
    ) -> AuditLogListResponse:
        """
        List audit log entries with optional filtering.

        Args:
            page: Page number (1-indexed)
            page_size: Number of items per page
            event_type: Filter by event type
            actor_id: Filter by actor user ID
            resource_type: Filter by resource type
            resource_id: Filter by resource ID
            start_date: Filter by start date
            end_date: Filter by end date

        Returns:
            List response with audit entries and pagination
        """
        params: dict[str, str | int] = {
            "page": page,
            "page_size": page_size,
        }
        if event_type:
            params["event_type"] = event_type
        if actor_id:
            params["actor_id"] = actor_id
        if resource_type:
            params["resource_type"] = resource_type
        if resource_id:
            params["resource_id"] = resource_id
        if start_date:
            params["start_date"] = start_date.isoformat()
        if end_date:
            params["end_date"] = end_date.isoformat()

        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.get(
                f"{self.base_url}/api/audit",
                params=params,
                headers=self._get_headers(),
            )
            response.raise_for_status()
            return AuditLogListResponse(**response.json())

    async def get(self, entry_id: str) -> AuditLogEntry:
        """
        Get a specific audit log entry.

        Args:
            entry_id: The audit log entry ID

        Returns:
            The audit log entry
        """
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.get(
                f"{self.base_url}/api/audit/{entry_id}",
                headers=self._get_headers(),
            )
            response.raise_for_status()
            return AuditLogEntry(**response.json())

    async def get_by_resource(
        self,
        resource_type: str,
        resource_id: str,
        page: int = 1,
        page_size: int = 20,
    ) -> AuditLogListResponse:
        """
        Get audit log entries for a specific resource.

        Args:
            resource_type: The resource type
            resource_id: The resource ID
            page: Page number
            page_size: Number of items per page

        Returns:
            List response with audit entries
        """
        return await self.list(
            page=page,
            page_size=page_size,
            resource_type=resource_type,
            resource_id=resource_id,
        )

    async def get_by_actor(
        self,
        actor_id: str,
        page: int = 1,
        page_size: int = 20,
    ) -> AuditLogListResponse:
        """
        Get audit log entries for a specific actor/user.

        Args:
            actor_id: The actor user ID
            page: Page number
            page_size: Number of items per page

        Returns:
            List response with audit entries
        """
        return await self.list(
            page=page,
            page_size=page_size,
            actor_id=actor_id,
        )
