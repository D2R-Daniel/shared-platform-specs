"""Tests for the users module."""
import pytest
from unittest.mock import MagicMock, patch, AsyncMock
from datetime import datetime

from shared_platform.users import UserClient, User, CreateUserRequest, UpdateUserRequest


class TestUser:
    """Tests for User model."""

    def test_user_from_dict(self, sample_user_dict):
        """Test creating User from dictionary."""
        user = User(**sample_user_dict)

        assert user.id == "user-123"
        assert user.email == "test@example.com"
        assert user.name == "Test User"
        assert user.status == "active"
        assert "user" in user.roles

    def test_user_defaults(self):
        """Test User with minimal data."""
        user = User(
            id="user-123",
            email="test@example.com",
            name="Test User",
        )

        assert user.id == "user-123"
        assert user.status is None
        assert user.roles is None


class TestCreateUserRequest:
    """Tests for CreateUserRequest."""

    def test_create_request_to_dict(self):
        """Test converting CreateUserRequest to dict."""
        request = CreateUserRequest(
            email="new@example.com",
            name="New User",
            roles=["user"],
            send_invitation=True,
        )

        data = request.model_dump(exclude_none=True)

        assert data["email"] == "new@example.com"
        assert data["name"] == "New User"
        assert data["roles"] == ["user"]
        assert data["send_invitation"] is True


class TestUpdateUserRequest:
    """Tests for UpdateUserRequest."""

    def test_update_request_partial(self):
        """Test partial update request."""
        request = UpdateUserRequest(name="Updated Name")

        data = request.model_dump(exclude_none=True)

        assert data["name"] == "Updated Name"
        assert "email" not in data
        assert "roles" not in data


class TestUserClient:
    """Tests for UserClient."""

    @pytest.fixture
    def client(self):
        """Create a UserClient instance."""
        return UserClient(
            base_url="https://api.example.com",
            access_token="test-token",
        )

    @pytest.mark.asyncio
    async def test_list_users(self, client, sample_user_dict):
        """Test listing users."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = {
                "data": [sample_user_dict],
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

            result = await client.list(page=1, page_size=20)

            assert len(result.data) == 1
            assert result.data[0].id == "user-123"
            assert result.pagination.total_items == 1

    @pytest.mark.asyncio
    async def test_get_user(self, client, sample_user_dict):
        """Test getting a single user."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = sample_user_dict
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.get = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            user = await client.get("user-123")

            assert user.id == "user-123"
            assert user.email == "test@example.com"

    @pytest.mark.asyncio
    async def test_create_user(self, client, sample_user_dict):
        """Test creating a user."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 201
            mock_response.json.return_value = sample_user_dict
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.post = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            request = CreateUserRequest(
                email="test@example.com",
                name="Test User",
                roles=["user"],
            )
            user = await client.create(request)

            assert user.id == "user-123"
            assert user.email == "test@example.com"

    @pytest.mark.asyncio
    async def test_update_user(self, client, sample_user_dict):
        """Test updating a user."""
        updated_user = {**sample_user_dict, "name": "Updated Name"}

        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = updated_user
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.patch = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            request = UpdateUserRequest(name="Updated Name")
            user = await client.update("user-123", request)

            assert user.name == "Updated Name"

    @pytest.mark.asyncio
    async def test_delete_user(self, client):
        """Test deleting a user."""
        with patch("httpx.AsyncClient") as mock_client:
            mock_response = MagicMock()
            mock_response.status_code = 204
            mock_response.raise_for_status = MagicMock()

            mock_instance = AsyncMock()
            mock_instance.delete = AsyncMock(return_value=mock_response)
            mock_client.return_value.__aenter__.return_value = mock_instance
            mock_client.return_value.__aexit__.return_value = None

            # Should not raise
            await client.delete("user-123")
