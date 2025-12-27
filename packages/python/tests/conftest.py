"""Pytest configuration and fixtures."""
import pytest
from unittest.mock import MagicMock, patch
import jwt
from datetime import datetime, timedelta


@pytest.fixture
def mock_httpx_response():
    """Create a mock httpx response."""
    def _create(status_code=200, json_data=None):
        response = MagicMock()
        response.status_code = status_code
        response.json.return_value = json_data or {}
        response.raise_for_status = MagicMock()
        return response
    return _create


@pytest.fixture
def sample_access_token():
    """Generate a sample JWT access token for testing."""
    payload = {
        "sub": "user-123",
        "email": "test@example.com",
        "name": "Test User",
        "tenant_id": "tenant-456",
        "roles": ["user", "admin"],
        "permissions": ["users:read", "users:write"],
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(hours=1),
    }
    return jwt.encode(payload, "secret", algorithm="HS256")


@pytest.fixture
def expired_access_token():
    """Generate an expired JWT access token for testing."""
    payload = {
        "sub": "user-123",
        "email": "test@example.com",
        "name": "Test User",
        "roles": ["user"],
        "permissions": [],
        "iat": datetime.utcnow() - timedelta(hours=2),
        "exp": datetime.utcnow() - timedelta(hours=1),
    }
    return jwt.encode(payload, "secret", algorithm="HS256")


@pytest.fixture
def sample_user_dict():
    """Sample user data dictionary."""
    return {
        "id": "user-123",
        "email": "test@example.com",
        "name": "Test User",
        "status": "active",
        "roles": ["user"],
        "tenant_id": "tenant-456",
        "created_at": "2024-01-01T00:00:00Z",
        "updated_at": "2024-01-01T00:00:00Z",
    }


@pytest.fixture
def sample_notification_dict():
    """Sample notification data dictionary."""
    return {
        "id": "notif-123",
        "type": "in_app",
        "category": "account",
        "title": "Welcome",
        "body": "Welcome to the platform!",
        "read": False,
        "created_at": "2024-01-01T00:00:00Z",
    }
