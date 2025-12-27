"""
Notification client.
"""

from __future__ import annotations

from typing import Optional
import httpx

from shared_platform.notifications.models import (
    Notification,
    NotificationListResponse,
    NotificationPreferences,
    NotificationCategory,
    ChannelSubscription,
    RegisteredDevice,
)


class NotificationClient:
    """
    Client for notification operations.

    Usage:
        notifications = NotificationClient(
            base_url="https://api.example.com",
            access_token="your-access-token",
        )

        # List notifications
        result = notifications.list(status="unread")

        # Mark as read
        notifications.mark_as_read(notification_id)

        # Get preferences
        prefs = notifications.get_preferences()
    """

    def __init__(
        self,
        base_url: str,
        access_token: Optional[str] = None,
        timeout: float = 30.0,
    ):
        self.base_url = base_url.rstrip("/")
        self._access_token = access_token
        self._http = httpx.Client(
            base_url=f"{self.base_url}/api/v1",
            timeout=timeout,
            headers=self._build_headers(),
        )

    def _build_headers(self) -> dict:
        headers = {"Content-Type": "application/json"}
        if self._access_token:
            headers["Authorization"] = f"Bearer {self._access_token}"
        return headers

    def set_access_token(self, token: str) -> None:
        """Update the access token."""
        self._access_token = token
        self._http.headers["Authorization"] = f"Bearer {token}"

    def list(
        self,
        page: int = 1,
        page_size: int = 20,
        status: str = "all",
        category: Optional[str] = None,
        notification_type: Optional[str] = None,
    ) -> NotificationListResponse:
        """
        List notifications.

        Args:
            page: Page number
            page_size: Items per page
            status: Filter by status (unread, read, all)
            category: Filter by category
            notification_type: Filter by type (email, sms, push, in_app)
        """
        params = {"page": page, "page_size": page_size, "status": status}
        if category:
            params["category"] = category
        if notification_type:
            params["type"] = notification_type

        response = self._http.get("/notifications", params=params)
        response.raise_for_status()
        return NotificationListResponse(**response.json())

    def get(self, notification_id: str) -> Notification:
        """Get a notification by ID."""
        response = self._http.get(f"/notifications/{notification_id}")
        response.raise_for_status()
        return Notification(**response.json())

    def delete(self, notification_id: str) -> None:
        """Delete a notification."""
        response = self._http.delete(f"/notifications/{notification_id}")
        response.raise_for_status()

    def mark_as_read(self, notification_id: str) -> Notification:
        """Mark a notification as read."""
        response = self._http.post(f"/notifications/{notification_id}/read")
        response.raise_for_status()
        return Notification(**response.json())

    def mark_all_as_read(
        self,
        category: Optional[str] = None,
        before: Optional[str] = None,
    ) -> int:
        """
        Mark all notifications as read.

        Returns the number of notifications updated.
        """
        data = {}
        if category:
            data["category"] = category
        if before:
            data["before"] = before

        response = self._http.post("/notifications/read-all", json=data)
        response.raise_for_status()
        return response.json().get("updated_count", 0)

    def get_unread_count(self) -> dict:
        """Get count of unread notifications."""
        response = self._http.get("/notifications/unread-count")
        response.raise_for_status()
        return response.json()

    # Preferences

    def get_preferences(self) -> NotificationPreferences:
        """Get notification preferences."""
        response = self._http.get("/notifications/preferences")
        response.raise_for_status()
        return NotificationPreferences(**response.json())

    def update_preferences(self, **kwargs) -> NotificationPreferences:
        """Update notification preferences."""
        response = self._http.put("/notifications/preferences", json=kwargs)
        response.raise_for_status()
        return NotificationPreferences(**response.json())

    def list_categories(self) -> list[NotificationCategory]:
        """List available notification categories."""
        response = self._http.get("/notifications/categories")
        response.raise_for_status()
        return [NotificationCategory(**c) for c in response.json().get("categories", [])]

    # Subscriptions

    def list_subscriptions(self) -> list[ChannelSubscription]:
        """List channel subscriptions."""
        response = self._http.get("/notifications/subscriptions")
        response.raise_for_status()
        return [ChannelSubscription(**s) for s in response.json().get("subscriptions", [])]

    def subscribe(self, channel: str, topic: str, endpoint: Optional[str] = None) -> ChannelSubscription:
        """Subscribe to a notification channel."""
        data = {"channel": channel, "topic": topic}
        if endpoint:
            data["endpoint"] = endpoint

        response = self._http.post("/notifications/subscriptions", json=data)
        response.raise_for_status()
        return ChannelSubscription(**response.json())

    def unsubscribe(self, subscription_id: str) -> None:
        """Unsubscribe from a channel."""
        response = self._http.delete(f"/notifications/subscriptions/{subscription_id}")
        response.raise_for_status()

    # Devices

    def list_devices(self) -> list[RegisteredDevice]:
        """List registered devices for push notifications."""
        response = self._http.get("/notifications/devices")
        response.raise_for_status()
        return [RegisteredDevice(**d) for d in response.json().get("devices", [])]

    def register_device(
        self,
        token: str,
        platform: str,
        name: Optional[str] = None,
        model: Optional[str] = None,
    ) -> RegisteredDevice:
        """Register a device for push notifications."""
        data = {"token": token, "platform": platform}
        if name:
            data["name"] = name
        if model:
            data["model"] = model

        response = self._http.post("/notifications/devices", json=data)
        response.raise_for_status()
        return RegisteredDevice(**response.json())

    def unregister_device(self, device_id: str) -> None:
        """Unregister a device."""
        response = self._http.delete(f"/notifications/devices/{device_id}")
        response.raise_for_status()

    # Test

    def send_test(self, channel: str, message: Optional[str] = None) -> dict:
        """Send a test notification."""
        data = {"channel": channel}
        if message:
            data["message"] = message

        response = self._http.post("/notifications/test", json=data)
        response.raise_for_status()
        return response.json()

    def close(self) -> None:
        """Close the HTTP client."""
        self._http.close()

    def __enter__(self) -> "NotificationClient":
        return self

    def __exit__(self, *args) -> None:
        self.close()
