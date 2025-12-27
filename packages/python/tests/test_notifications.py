"""Tests for the notifications module."""
import pytest
from unittest.mock import MagicMock, patch
from datetime import datetime

from shared_platform.notifications import (
    NotificationClient,
    Notification,
    NotificationPreferences,
)
from shared_platform.notifications.events import (
    EmailNotificationEvent,
    SMSNotificationEvent,
    PushNotificationEvent,
    EmailRecipient,
    EmailTemplate,
    SMSRecipient,
    PushTarget,
    PushNotification,
    EventSource,
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
            created_at=datetime.now(),
        )

        assert notification.id == "notif-123"
        assert notification.body is None
        assert notification.read is False


class TestNotificationPreferences:
    """Tests for NotificationPreferences model."""

    def test_preferences_defaults(self):
        """Test default preferences."""
        prefs = NotificationPreferences()

        assert prefs.email_enabled is True
        assert prefs.push_enabled is True
        assert prefs.digest_frequency == "realtime"

    def test_preferences_with_values(self):
        """Test preferences with values."""
        prefs = NotificationPreferences(
            email_enabled=False,
            push_enabled=True,
            digest_frequency="daily",
        )

        assert prefs.email_enabled is False
        assert prefs.push_enabled is True
        assert prefs.digest_frequency == "daily"


class TestNotificationEvents:
    """Tests for notification event models."""

    def test_email_event(self):
        """Test EmailNotificationEvent creation."""
        event = EmailNotificationEvent(
            event_id="evt-123",
            event_type="TRANSACTIONAL",
            tenant_id="tenant-456",
            recipient=EmailRecipient(email="user@example.com", name="Test User"),
            template=EmailTemplate(template_id="welcome", variables={"name": "Test"}),
            category="account",
            source=EventSource(service="user-service", action="user_registered"),
        )

        assert event.event_id == "evt-123"
        assert event.event_type.value == "TRANSACTIONAL"
        assert event.recipient.email == "user@example.com"

    def test_sms_event(self):
        """Test SMSNotificationEvent creation."""
        event = SMSNotificationEvent(
            event_id="evt-123",
            event_type="OTP",
            tenant_id="tenant-456",
            recipient=SMSRecipient(phone_number="+1234567890"),
            message="Your OTP is 123456",
            category="security",
            source=EventSource(service="auth-service", action="otp_requested"),
        )

        assert event.event_id == "evt-123"
        assert event.event_type.value == "OTP"
        assert event.message == "Your OTP is 123456"

    def test_push_event(self):
        """Test PushNotificationEvent creation."""
        event = PushNotificationEvent(
            event_id="evt-123",
            event_type="ALERT",
            tenant_id="tenant-456",
            target=PushTarget(user_id="user-123"),
            notification=PushNotification(title="Alert", body="Something happened"),
            category="alerts",
            source=EventSource(service="monitor-service", action="alert_triggered"),
        )

        assert event.event_id == "evt-123"
        assert event.event_type.value == "ALERT"
        assert event.notification.title == "Alert"

    def test_event_to_dict(self):
        """Test event serialization."""
        event = EmailNotificationEvent(
            event_id="evt-123",
            tenant_id="tenant-456",
            recipient=EmailRecipient(email="user@example.com"),
            template=EmailTemplate(template_id="welcome"),
            category="account",
            source=EventSource(service="test", action="test"),
        )

        data = event.to_dict()

        assert isinstance(data, dict)
        assert data["event_id"] == "evt-123"
        assert data["recipient"]["email"] == "user@example.com"


class TestNotificationClient:
    """Tests for NotificationClient."""

    @pytest.fixture
    def client(self):
        """Create a NotificationClient instance with mocked HTTP."""
        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_client.return_value = mock_instance
            notif_client = NotificationClient(
                base_url="https://api.example.com",
                access_token="test-token",
            )
            notif_client._http = mock_instance
            yield notif_client

    def test_list_notifications(self, client, sample_notification_dict, mock_httpx_response):
        """Test listing notifications."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "data": [sample_notification_dict],
                "pagination": {
                    "page": 1,
                    "page_size": 20,
                    "total_items": 1,
                    "total_pages": 1,
                    "has_next": False,
                    "has_previous": False,
                },
            },
        )
        client._http.get.return_value = mock_response

        result = client.list()

        assert len(result.data) == 1
        assert result.data[0].id == "notif-123"

    def test_get_notification(self, client, sample_notification_dict, mock_httpx_response):
        """Test getting a single notification."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data=sample_notification_dict,
        )
        client._http.get.return_value = mock_response

        notification = client.get("notif-123")

        assert notification.id == "notif-123"
        assert notification.title == "Welcome"

    def test_mark_as_read(self, client, sample_notification_dict, mock_httpx_response):
        """Test marking notification as read."""
        read_notification = {**sample_notification_dict, "read": True}
        mock_response = mock_httpx_response(status_code=200, json_data=read_notification)
        client._http.post.return_value = mock_response

        notification = client.mark_as_read("notif-123")

        assert notification.read is True

    def test_mark_all_as_read(self, client, mock_httpx_response):
        """Test marking all notifications as read."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={"updated_count": 5},
        )
        client._http.post.return_value = mock_response

        count = client.mark_all_as_read()

        assert count == 5

    def test_delete_notification(self, client, mock_httpx_response):
        """Test deleting a notification."""
        mock_response = mock_httpx_response(status_code=204)
        client._http.delete.return_value = mock_response

        # Should not raise
        client.delete("notif-123")

    def test_get_unread_count(self, client, mock_httpx_response):
        """Test getting unread notification count."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={"count": 5},
        )
        client._http.get.return_value = mock_response

        result = client.get_unread_count()

        assert result["count"] == 5

    def test_get_preferences(self, client, mock_httpx_response):
        """Test getting notification preferences."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "email_enabled": True,
                "push_enabled": True,
                "digest_frequency": "daily",
            },
        )
        client._http.get.return_value = mock_response

        prefs = client.get_preferences()

        assert prefs.email_enabled is True
        assert prefs.digest_frequency == "daily"

    def test_update_preferences(self, client, mock_httpx_response):
        """Test updating notification preferences."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "email_enabled": False,
                "push_enabled": True,
                "digest_frequency": "weekly",
            },
        )
        client._http.put.return_value = mock_response

        prefs = client.update_preferences(email_enabled=False, digest_frequency="weekly")

        assert prefs.email_enabled is False
        assert prefs.digest_frequency == "weekly"

    def test_list_categories(self, client, mock_httpx_response):
        """Test listing notification categories."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "categories": [
                    {
                        "id": "account",
                        "name": "Account",
                        "description": "Account-related notifications",
                    }
                ]
            },
        )
        client._http.get.return_value = mock_response

        categories = client.list_categories()

        assert len(categories) == 1
        assert categories[0].id == "account"

    def test_list_subscriptions(self, client, mock_httpx_response):
        """Test listing subscriptions."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "subscriptions": [
                    {
                        "id": "sub-123",
                        "channel": "email",
                        "topic": "alerts",
                        "subscribed_at": "2024-01-01T00:00:00Z",
                    }
                ]
            },
        )
        client._http.get.return_value = mock_response

        subscriptions = client.list_subscriptions()

        assert len(subscriptions) == 1
        assert subscriptions[0].channel == "email"

    def test_subscribe(self, client, mock_httpx_response):
        """Test subscribing to a channel."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "id": "sub-123",
                "channel": "push",
                "topic": "updates",
                "subscribed_at": "2024-01-01T00:00:00Z",
            },
        )
        client._http.post.return_value = mock_response

        subscription = client.subscribe("push", "updates")

        assert subscription.channel == "push"
        assert subscription.topic == "updates"

    def test_unsubscribe(self, client, mock_httpx_response):
        """Test unsubscribing from a channel."""
        mock_response = mock_httpx_response(status_code=204)
        client._http.delete.return_value = mock_response

        # Should not raise
        client.unsubscribe("sub-123")

    def test_list_devices(self, client, mock_httpx_response):
        """Test listing registered devices."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "devices": [
                    {
                        "id": "device-123",
                        "platform": "android",
                        "name": "My Phone",
                        "registered_at": "2024-01-01T00:00:00Z",
                    }
                ]
            },
        )
        client._http.get.return_value = mock_response

        devices = client.list_devices()

        assert len(devices) == 1
        assert devices[0].platform == "android"

    def test_register_device(self, client, mock_httpx_response):
        """Test registering a device for push notifications."""
        mock_response = mock_httpx_response(
            status_code=201,
            json_data={
                "id": "device-123",
                "platform": "android",
                "name": "My Phone",
                "registered_at": "2024-01-01T00:00:00Z",
            },
        )
        client._http.post.return_value = mock_response

        device = client.register_device(
            token="fcm-token",
            platform="android",
            name="My Phone",
        )

        assert device.id == "device-123"
        assert device.platform == "android"

    def test_unregister_device(self, client, mock_httpx_response):
        """Test unregistering a device."""
        mock_response = mock_httpx_response(status_code=204)
        client._http.delete.return_value = mock_response

        # Should not raise
        client.unregister_device("device-123")

    def test_send_test(self, client, mock_httpx_response):
        """Test sending a test notification."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={"message": "Test notification sent"},
        )
        client._http.post.return_value = mock_response

        result = client.send_test("push", "Hello!")

        assert "message" in result

    def test_set_access_token(self):
        """Test setting access token."""
        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_instance.headers = {}  # Use real dict for headers
            mock_client.return_value = mock_instance

            client = NotificationClient(
                base_url="https://api.example.com",
                access_token="test-token",
            )
            client._http = mock_instance
            client._http.headers = {"Authorization": "Bearer test-token"}

            client.set_access_token("new-token")
            assert client._http.headers["Authorization"] == "Bearer new-token"

    def test_context_manager(self, mock_httpx_response):
        """Test NotificationClient as context manager."""
        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_client.return_value = mock_instance

            with NotificationClient(
                base_url="https://api.example.com",
                access_token="test-token",
            ) as client:
                assert client is not None

            mock_instance.close.assert_called_once()
