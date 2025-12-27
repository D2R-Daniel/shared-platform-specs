"""Tests for the users module."""
import pytest
from unittest.mock import MagicMock, patch
from datetime import datetime

from shared_platform.users import UserClient, User, CreateUserRequest, UpdateUserRequest
from shared_platform.users.models import UserListResponse, Pagination


class TestUser:
    """Tests for User model."""

    def test_user_from_dict(self, sample_user_dict):
        """Test creating User from dictionary."""
        user = User(**sample_user_dict)

        assert user.id == "user-123"
        assert user.email == "test@example.com"
        assert user.name == "Test User"
        assert user.status.value == "active"
        assert "user" in user.roles

    def test_user_defaults(self):
        """Test User with minimal data."""
        user = User(
            id="user-123",
            email="test@example.com",
            created_at=datetime.now(),
            updated_at=datetime.now(),
        )

        assert user.id == "user-123"
        assert user.name is None
        assert user.roles == []


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
        """Create a UserClient instance with mocked HTTP."""
        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_client.return_value = mock_instance
            user_client = UserClient(
                base_url="https://api.example.com",
                access_token="test-token",
            )
            user_client._http = mock_instance
            yield user_client

    def test_list_users(self, client, sample_user_dict, mock_httpx_response):
        """Test listing users."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "data": [sample_user_dict],
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

        result = client.list(page=1, page_size=20)

        assert len(result.data) == 1
        assert result.data[0].id == "user-123"
        assert result.pagination.total_items == 1

    def test_get_user(self, client, sample_user_dict, mock_httpx_response):
        """Test getting a single user."""
        mock_response = mock_httpx_response(status_code=200, json_data=sample_user_dict)
        client._http.get.return_value = mock_response

        user = client.get("user-123")

        assert user.id == "user-123"
        assert user.email == "test@example.com"

    def test_create_user(self, client, sample_user_dict, mock_httpx_response):
        """Test creating a user."""
        mock_response = mock_httpx_response(status_code=201, json_data=sample_user_dict)
        client._http.post.return_value = mock_response

        request = CreateUserRequest(
            email="test@example.com",
            name="Test User",
            roles=["user"],
        )
        user = client.create(request)

        assert user.id == "user-123"
        assert user.email == "test@example.com"

    def test_update_user(self, client, sample_user_dict, mock_httpx_response):
        """Test updating a user."""
        updated_user = {**sample_user_dict, "name": "Updated Name"}
        mock_response = mock_httpx_response(status_code=200, json_data=updated_user)
        client._http.put.return_value = mock_response

        request = UpdateUserRequest(name="Updated Name")
        user = client.update("user-123", request)

        assert user.name == "Updated Name"

    def test_delete_user(self, client, mock_httpx_response):
        """Test deleting a user."""
        mock_response = mock_httpx_response(status_code=204)
        client._http.delete.return_value = mock_response

        # Should not raise
        client.delete("user-123")

    def test_update_status(self, client, sample_user_dict, mock_httpx_response):
        """Test updating user status."""
        suspended_user = {**sample_user_dict, "status": "suspended"}
        mock_response = mock_httpx_response(status_code=200, json_data=suspended_user)
        client._http.patch.return_value = mock_response

        user = client.update_status("user-123", "suspended", reason="Policy violation")

        assert user.status.value == "suspended"

    def test_update_roles(self, client, sample_user_dict, mock_httpx_response):
        """Test updating user roles."""
        updated_user = {**sample_user_dict, "roles": ["admin", "user"]}
        mock_response = mock_httpx_response(status_code=200, json_data=updated_user)
        client._http.put.return_value = mock_response

        user = client.update_roles("user-123", ["admin", "user"])

        assert "admin" in user.roles
        assert "user" in user.roles

    def test_reset_password(self, client, mock_httpx_response):
        """Test resetting user password."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={"message": "Password reset email sent"},
        )
        client._http.post.return_value = mock_response

        result = client.reset_password("user-123", send_email=True)

        assert "message" in result

    def test_get_my_profile(self, client, mock_httpx_response):
        """Test getting current user profile."""
        profile_data = {
            "id": "user-123",
            "email": "test@example.com",
            "name": "Test User",
            "roles": ["user"],
            "created_at": "2024-01-01T00:00:00Z",
            "updated_at": "2024-01-01T00:00:00Z",
        }
        mock_response = mock_httpx_response(status_code=200, json_data=profile_data)
        client._http.get.return_value = mock_response

        profile = client.get_my_profile()

        assert profile.id == "user-123"
        assert profile.email == "test@example.com"

    def test_change_password(self, client, mock_httpx_response):
        """Test changing password."""
        mock_response = mock_httpx_response(status_code=200)
        client._http.post.return_value = mock_response

        # Should not raise
        client.change_password("old-password", "new-password")

    def test_get_stats(self, client, mock_httpx_response):
        """Test getting user stats."""
        mock_response = mock_httpx_response(
            status_code=200,
            json_data={
                "total_users": 100,
                "active_users": 80,
                "pending_users": 15,
                "suspended_users": 5,
                "users_by_role": {"user": 70, "admin": 10},
            },
        )
        client._http.get.return_value = mock_response

        stats = client.get_stats()

        assert stats.total_users == 100
        assert stats.active_users == 80

    def test_set_access_token(self):
        """Test setting access token."""
        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_instance.headers = {}  # Use real dict for headers
            mock_client.return_value = mock_instance

            client = UserClient(
                base_url="https://api.example.com",
                access_token="test-token",
            )
            client._http = mock_instance
            client._http.headers = {"Authorization": "Bearer test-token"}

            client.set_access_token("new-token")
            assert client._http.headers["Authorization"] == "Bearer new-token"

    def test_context_manager(self, mock_httpx_response):
        """Test UserClient as context manager."""
        with patch("httpx.Client") as mock_client:
            mock_instance = MagicMock()
            mock_client.return_value = mock_instance

            with UserClient(
                base_url="https://api.example.com",
                access_token="test-token",
            ) as client:
                assert client is not None

            mock_instance.close.assert_called_once()
