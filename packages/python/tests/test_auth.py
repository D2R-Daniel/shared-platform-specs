"""Tests for the auth module."""
import pytest
from datetime import datetime, timedelta
from unittest.mock import MagicMock, patch, AsyncMock
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

    def test_from_token_valid(self, sample_access_token):
        """Test creating UserContext from a valid token."""
        context = UserContext.from_token(sample_access_token)

        assert context.user_id == "user-123"
        assert context.email == "test@example.com"
        assert context.name == "Test User"
        assert context.tenant_id == "tenant-456"
        assert "user" in context.roles
        assert "admin" in context.roles

    def test_from_token_expired(self, expired_access_token):
        """Test that expired tokens raise TokenExpiredError."""
        with pytest.raises(TokenExpiredError):
            UserContext.from_token(expired_access_token)

    def test_from_token_invalid(self):
        """Test that invalid tokens raise InvalidTokenError."""
        with pytest.raises(InvalidTokenError):
            UserContext.from_token("invalid-token")

    def test_has_permission_direct(self, sample_access_token):
        """Test permission checking with direct permission."""
        context = UserContext.from_token(sample_access_token)

        assert context.has_permission("users:read") is True
        assert context.has_permission("users:write") is True
        assert context.has_permission("settings:read") is False

    def test_has_permission_wildcard(self, sample_access_token):
        """Test permission checking with wildcard."""
        # Create a token with wildcard permission
        payload = {
            "sub": "user-123",
            "email": "admin@example.com",
            "name": "Admin User",
            "roles": ["admin"],
            "permissions": ["users:*"],
            "iat": datetime.utcnow(),
            "exp": datetime.utcnow() + timedelta(hours=1),
        }
        token = jwt.encode(payload, "secret", algorithm="HS256")
        context = UserContext.from_token(token)

        assert context.has_permission("users:read") is True
        assert context.has_permission("users:write") is True
        assert context.has_permission("users:delete") is True

    def test_has_role(self, sample_access_token):
        """Test role checking."""
        context = UserContext.from_token(sample_access_token)

        assert context.has_role("user") is True
        assert context.has_role("admin") is True
        assert context.has_role("super_admin") is False

    def test_is_admin(self, sample_access_token):
        """Test admin check."""
        context = UserContext.from_token(sample_access_token)
        assert context.is_admin() is True

    def test_is_not_admin(self):
        """Test non-admin user."""
        payload = {
            "sub": "user-123",
            "email": "user@example.com",
            "name": "Regular User",
            "roles": ["user"],
            "permissions": [],
            "iat": datetime.utcnow(),
            "exp": datetime.utcnow() + timedelta(hours=1),
        }
        token = jwt.encode(payload, "secret", algorithm="HS256")
        context = UserContext.from_token(token)

        assert context.is_admin() is False


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
        assert "notifications:*" in perms
        assert "users:*" not in perms

    def test_check_permission_exact_match(self):
        """Test exact permission match."""
        granted = {"users:read", "users:write"}

        assert check_permission(granted, "users:read") is True
        assert check_permission(granted, "users:write") is True
        assert check_permission(granted, "users:delete") is False

    def test_check_permission_wildcard(self):
        """Test wildcard permission matching."""
        granted = {"users:*", "reports:read"}

        assert check_permission(granted, "users:read") is True
        assert check_permission(granted, "users:write") is True
        assert check_permission(granted, "users:delete") is True
        assert check_permission(granted, "reports:read") is True
        assert check_permission(granted, "reports:write") is False

    def test_check_permission_super_wildcard(self):
        """Test super wildcard (*) matches everything."""
        granted = {"*"}

        assert check_permission(granted, "users:read") is True
        assert check_permission(granted, "settings:write") is True
        assert check_permission(granted, "anything:anything") is True


class TestAuthClient:
    """Tests for AuthClient."""

    @pytest.mark.asyncio
    async def test_get_user_context(self, sample_access_token):
        """Test getting user context from token."""
        client = AuthClient(issuer_url="https://auth.example.com")
        context = client.get_user_context(sample_access_token)

        assert context.user_id == "user-123"
        assert context.email == "test@example.com"

    @pytest.mark.asyncio
    async def test_login_success(self):
        """Test successful login."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = {
                "access_token": "test-access-token",
                "refresh_token": "test-refresh-token",
                "token_type": "Bearer",
                "expires_in": 3600,
            }
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.post = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            client = AuthClient(issuer_url="https://auth.example.com")
            tokens = await client.login("user@example.com", "password")

            assert tokens.access_token == "test-access-token"
            assert tokens.refresh_token == "test-refresh-token"
