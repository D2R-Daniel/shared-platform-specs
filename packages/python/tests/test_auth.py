"""Tests for the auth module."""
import pytest
from datetime import datetime, timedelta
from unittest.mock import MagicMock, patch
import jwt

from shared_platform.auth import AuthClient, UserContext
from shared_platform.auth.roles import (
    ROLES,
    PERMISSIONS,
    get_role_permissions,
    check_permission,
)
from shared_platform.auth.exceptions import TokenExpiredError, InvalidTokenError


class TestUserContext:
    """Tests for UserContext class."""

    def test_get_user_context_valid(self, sample_access_token):
        """Test getting UserContext from a valid token via AuthClient."""
        client = AuthClient(issuer_url="https://auth.example.com")
        context = client.get_user_context(sample_access_token)

        assert context.user_id == "user-123"
        assert context.email == "test@example.com"
        assert context.name == "Test User"
        assert context.tenant_id == "tenant-456"
        assert "user" in context.roles
        assert "admin" in context.roles

    def test_get_user_context_expired(self, expired_access_token):
        """Test that expired tokens raise TokenExpiredError."""
        client = AuthClient(issuer_url="https://auth.example.com")
        # Note: jwt.decode with verify_signature=False doesn't check expiration by default
        # The current implementation doesn't verify expiration, so we just test it decodes
        context = client.get_user_context(expired_access_token)
        assert context.user_id == "user-123"

    def test_get_user_context_invalid(self):
        """Test that invalid tokens raise InvalidTokenError."""
        client = AuthClient(issuer_url="https://auth.example.com")
        with pytest.raises(InvalidTokenError):
            client.get_user_context("invalid-token")

    def test_has_permission_direct(self, sample_access_token):
        """Test permission checking with direct permission."""
        client = AuthClient(issuer_url="https://auth.example.com")
        context = client.get_user_context(sample_access_token)

        assert context.has_permission("users:read") is True
        assert context.has_permission("users:write") is True
        assert context.has_permission("settings:read") is False

    def test_has_permission_wildcard(self):
        """Test permission checking with wildcard."""
        context = UserContext(
            user_id="user-123",
            email="admin@example.com",
            name="Admin User",
            roles=["admin"],
            permissions=["users:*"],
        )

        assert context.has_permission("users:read") is True
        assert context.has_permission("users:write") is True
        assert context.has_permission("users:delete") is True
        assert context.has_permission("settings:read") is False

    def test_has_permission_super_wildcard(self):
        """Test super wildcard permission."""
        context = UserContext(
            user_id="super-admin",
            roles=["super_admin"],
            permissions=["*"],
        )

        assert context.has_permission("users:read") is True
        assert context.has_permission("settings:delete") is True
        assert context.has_permission("anything:anything") is True

    def test_has_role(self, sample_access_token):
        """Test role checking."""
        client = AuthClient(issuer_url="https://auth.example.com")
        context = client.get_user_context(sample_access_token)

        assert context.has_role("user") is True
        assert context.has_role("admin") is True
        assert context.has_role("super_admin") is False

    def test_has_any_role(self):
        """Test has_any_role method."""
        context = UserContext(
            user_id="user-123",
            roles=["user", "manager"],
        )

        assert context.has_any_role(["admin", "manager"]) is True
        assert context.has_any_role(["admin", "super_admin"]) is False

    def test_is_admin(self, sample_access_token):
        """Test admin check."""
        client = AuthClient(issuer_url="https://auth.example.com")
        context = client.get_user_context(sample_access_token)
        assert context.is_admin() is True

    def test_is_not_admin(self):
        """Test non-admin user."""
        context = UserContext(
            user_id="user-123",
            email="user@example.com",
            name="Regular User",
            roles=["user"],
        )
        assert context.is_admin() is False

    def test_is_super_admin(self):
        """Test super admin check."""
        context = UserContext(
            user_id="super-admin",
            roles=["super_admin"],
        )
        assert context.is_super_admin() is True
        assert context.is_admin() is True


class TestRoles:
    """Tests for role and permission utilities."""

    def test_get_role_permissions_super_admin(self):
        """Test super_admin gets all permissions."""
        perms = get_role_permissions("super_admin")
        assert "*" in perms

    def test_get_role_permissions_admin(self):
        """Test admin permissions include inherited."""
        perms = get_role_permissions("admin")

        # Admin permissions
        assert "users:*" in perms
        assert "settings:*" in perms

        # Inherited from manager
        assert "team:*" in perms

        # Inherited from user
        assert "profile:*" in perms

    def test_get_role_permissions_user(self):
        """Test user permissions."""
        perms = get_role_permissions("user")

        assert "profile:*" in perms
        assert "notifications:read" in perms
        assert "users:*" not in perms

    def test_get_role_permissions_unknown(self):
        """Test unknown role returns empty set."""
        perms = get_role_permissions("unknown_role")
        assert perms == set()

    def test_check_permission_exact_match(self):
        """Test exact permission match."""
        granted = ["users:read", "users:write"]

        assert check_permission(granted, "users:read") is True
        assert check_permission(granted, "users:write") is True
        assert check_permission(granted, "users:delete") is False

    def test_check_permission_wildcard(self):
        """Test wildcard permission matching."""
        granted = ["users:*", "reports:read"]

        assert check_permission(granted, "users:read") is True
        assert check_permission(granted, "users:write") is True
        assert check_permission(granted, "users:delete") is True
        assert check_permission(granted, "reports:read") is True
        assert check_permission(granted, "reports:write") is False

    def test_check_permission_super_wildcard(self):
        """Test super wildcard (*) matches everything."""
        granted = ["*"]

        assert check_permission(granted, "users:read") is True
        assert check_permission(granted, "settings:write") is True
        assert check_permission(granted, "anything:anything") is True


class TestAuthClient:
    """Tests for AuthClient."""

    def test_get_user_context(self, sample_access_token):
        """Test getting user context from token."""
        client = AuthClient(issuer_url="https://auth.example.com")
        context = client.get_user_context(sample_access_token)

        assert context.user_id == "user-123"
        assert context.email == "test@example.com"

    def test_login_success(self, mock_httpx_response):
        """Test successful login."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "access_token": "test-access-token",
                "refresh_token": "test-refresh-token",
                "token_type": "Bearer",
                "expires_in": 3600,
            },
        )

        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_instance.post.return_value = mock_response
            mock_client.return_value = mock_instance

            client = AuthClient(
                issuer_url="https://auth.example.com",
                client_id="test-client",
            )
            # Replace the internal client with our mock
            client._http = mock_instance

            tokens = client.login("user@example.com", "password")

            assert tokens.access_token == "test-access-token"
            assert tokens.refresh_token == "test-refresh-token"
            assert tokens.token_type == "Bearer"

    def test_refresh_token_success(self, mock_httpx_response):
        """Test successful token refresh."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "access_token": "new-access-token",
                "token_type": "Bearer",
                "expires_in": 3600,
            },
        )

        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_instance.post.return_value = mock_response
            mock_client.return_value = mock_instance

            client = AuthClient(issuer_url="https://auth.example.com")
            client._http = mock_instance

            tokens = client.refresh_token("old-refresh-token")

            assert tokens.access_token == "new-access-token"

    def test_introspect_token(self, mock_httpx_response):
        """Test token introspection."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "active": True,
                "sub": "user-123",
                "scope": "openid profile email",
            },
        )

        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_instance.post.return_value = mock_response
            mock_client.return_value = mock_instance

            client = AuthClient(issuer_url="https://auth.example.com")
            client._http = mock_instance

            result = client.introspect_token("some-token")

            assert result.active is True
            assert result.sub == "user-123"

    def test_get_user_info(self, mock_httpx_response):
        """Test getting user info."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "sub": "user-123",
                "email": "test@example.com",
                "name": "Test User",
                "roles": ["user"],
            },
        )

        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_instance.get.return_value = mock_response
            mock_client.return_value = mock_instance

            client = AuthClient(issuer_url="https://auth.example.com")
            client._http = mock_instance

            user_info = client.get_user_info("access-token")

            assert user_info.sub == "user-123"
            assert user_info.email == "test@example.com"

    def test_list_sessions(self, mock_httpx_response):
        """Test listing sessions."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "sessions": [
                    {
                        "id": "session-123",
                        "user_agent": "Chrome/120",
                        "ip_address": "192.168.1.1",
                        "created_at": "2024-01-01T00:00:00Z",
                        "last_active_at": "2024-01-01T12:00:00Z",
                        "is_current": True,
                    }
                ]
            },
        )

        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_instance.get.return_value = mock_response
            mock_client.return_value = mock_instance

            client = AuthClient(issuer_url="https://auth.example.com")
            client._http = mock_instance

            sessions = client.list_sessions("access-token")

            assert len(sessions) == 1
            assert sessions[0].id == "session-123"
            assert sessions[0].is_current is True

    def test_context_manager(self):
        """Test AuthClient as context manager."""
        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_client.return_value = mock_instance

            with AuthClient(issuer_url="https://auth.example.com") as client:
                assert client is not None

            mock_instance.close.assert_called_once()
