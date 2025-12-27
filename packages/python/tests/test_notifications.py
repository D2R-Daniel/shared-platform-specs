"""Tests for the notifications module."""
import pytest
from unittest.mock import MagicMock, patch, AsyncMock

from shared_platform.notifications import (
    NotificationClient,
    Notification,
    NotificationPreferences,
)
from shared_platform.notifications.events import (
    EmailNotificationEvent,
    SMSNotificationEvent,
    PushNotificationEvent,
)


class TestNotification:
    """Tests for Notification model."""

    def test_notification_from_dict(self, sample_notification_dict):
        """Test creating Notification from dictionary."""
        notification = Notification(**sample_notification_dict)

        assert notification.id == "notif-123"
        assert notification.type == "in_app"
        assert notification.category == "account"
        assert notification.title == "Welcome"
        assert notification.read is False

    def test_notification_defaults(self):
        """Test Notification with minimal data."""
        notification = Notification(
            id="notif-123",
            type="in_app",
            category="account",
            title="Test",
            read=False,
        )

        assert notification.id == "notif-123"
        assert notification.body is None


class TestNotificationPreferences:
    """Tests for NotificationPreferences model."""

    def test_preferences_defaults(self):
        """Test default preferences."""
        prefs = NotificationPreferences()

        assert prefs.email_enabled is None
        assert prefs.push_enabled is None

    def test_preferences_with_values(self):
        """Test preferences with values."""
        prefs = NotificationPreferences(
            email_enabled=True,
            push_enabled=True,
            digest_frequency="daily",
        )

        assert prefs.email_enabled is True
        assert prefs.push_enabled is True
        assert prefs.digest_frequency == "daily"


class TestNotificationEvents:
    """Tests for notification event models."""

    def test_email_event(self):
        """Test EmailNotificationEvent creation."""
        event = EmailNotificationEvent(
            event_id="evt-123",
            event_type="TRANSACTIONAL",
            timestamp="2024-01-01T00:00:00Z",
            tenant_id="tenant-456",
            recipient={"email": "user@example.com", "name": "Test User"},
            template={"template_id": "welcome", "variables": {"name": "Test"}},
            category="account",
            source={"service": "user-service", "action": "user_registered"},
        )

        assert event.event_id == "evt-123"
        assert event.event_type == "TRANSACTIONAL"
        assert event.recipient["email"] == "user@example.com"

    def test_sms_event(self):
        """Test SMSNotificationEvent creation."""
        event = SMSNotificationEvent(
            event_id="evt-123",
            event_type="OTP",
            timestamp="2024-01-01T00:00:00Z",
            tenant_id="tenant-456",
            recipient={"phone_number": "+1234567890"},
            message="Your OTP is 123456",
            category="security",
            source={"service": "auth-service", "action": "otp_requested"},
        )

        assert event.event_id == "evt-123"
        assert event.event_type == "OTP"
        assert event.message == "Your OTP is 123456"

    def test_push_event(self):
        """Test PushNotificationEvent creation."""
        event = PushNotificationEvent(
            event_id="evt-123",
            event_type="ALERT",
            timestamp="2024-01-01T00:00:00Z",
            tenant_id="tenant-456",
            target={"user_id": "user-123"},
            notification={"title": "Alert", "body": "Something happened"},
            category="alerts",
            source={"service": "monitor-service", "action": "alert_triggered"},
        )

        assert event.event_id == "evt-123"
        assert event.event_type == "ALERT"
        assert event.notification["title"] == "Alert"


class TestNotificationClient:
    """Tests for NotificationClient."""

    @pytest.fixture
    def client(self):
        """Create a NotificationClient instance."""
        return NotificationClient(
            base_url="https://api.example.com",
            access_token="test-token",
        )

    @pytest.mark.asyncio
    async def test_list_notifications(self, client, sample_notification_dict):
        """Test listing notifications."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = {
                "data": [sample_notification_dict],
                "pagination": {
                    "page": 1,
                    "page_size": 20,
                    "total_items": 1,
                    "total_pages": 1,
                },
            }
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.get = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            result = await client.list()

            assert len(result.data) == 1
            assert result.data[0].id == "notif-123"

    @pytest.mark.asyncio
    async def test_mark_as_read(self, client, sample_notification_dict):
        """Test marking notification as read."""
        read_notification = {**sample_notification_dict, "read": True}

        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = read_notification
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.patch = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            notification = await client.mark_as_read("notif-123")

            assert notification.read is True

    @pytest.mark.asyncio
    async def test_get_unread_count(self, client):
        """Test getting unread notification count."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = {"count": 5}
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.get = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            result = await client.get_unread_count()

            assert result["count"] == 5

    @pytest.mark.asyncio
    async def test_get_preferences(self, client):
        """Test getting notification preferences."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = {
                "email_enabled": True,
                "push_enabled": True,
                "digest_frequency": "daily",
            }
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.get = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            prefs = await client.get_preferences()

            assert prefs.email_enabled is True
            assert prefs.digest_frequency == "daily"

    @pytest.mark.asyncio
    async def test_register_device(self, client):
        """Test registering a device for push notifications."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 201
            mock_response.json.return_value = {
                "id": "device-123",
                "platform": "android",
                "name": "My Phone",
                "registered_at": "2024-01-01T00:00:00Z",
            }
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.post = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            device = await client.register_device(
                token="fcm-token",
                platform="android",
                name="My Phone",
            )

            assert device.id == "device-123"
            assert device.platform == "android"
