"""
User management client.
"""

from __future__ import annotations

from typing import Optional
import httpx

from shared_platform.users.models import (
    User,
    UserProfile,
    UserPreferences,
    CreateUserRequest,
    UpdateUserRequest,
    InviteUserRequest,
    UserListResponse,
    UserStats,
)


class UserClient:
    """
    Client for user management operations.

    Usage:
        users = UserClient(
            base_url="https://api.example.com",
            access_token="your-access-token",
        )

        # List users
        result = users.list(page=1, page_size=20)
        for user in result.data:
            print(user.email)

        # Get a user
        user = users.get("user-id")

        # Create a user
        new_user = users.create(CreateUserRequest(
            email="new@example.com",
            name="New User",
            roles=["user"],
        ))
    """

    def __init__(
        self,
        base_url: str,
        access_token: Optional[str] = None,
        timeout: float = 30.0,
    ):
        """
        Initialize the user client.

        Args:
            base_url: Base URL of the API server
            access_token: JWT access token for authentication
            timeout: Request timeout in seconds
        """
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
        search: Optional[str] = None,
        status: Optional[str] = None,
        role: Optional[str] = None,
        team_id: Optional[str] = None,
        sort: str = "-created_at",
    ) -> UserListResponse:
        """
        List users with optional filtering.

        Args:
            page: Page number (1-indexed)
            page_size: Items per page (max 100)
            search: Search in name, email
            status: Filter by status (active, inactive, pending, suspended)
            role: Filter by role
            team_id: Filter by team
            sort: Sort field (prefix with - for descending)

        Returns:
            UserListResponse with paginated users
        """
        params = {
            "page": page,
            "page_size": page_size,
            "sort": sort,
        }
        if search:
            params["search"] = search
        if status:
            params["status"] = status
        if role:
            params["role"] = role
        if team_id:
            params["team_id"] = team_id

        response = self._http.get("/users", params=params)
        response.raise_for_status()
        return UserListResponse(**response.json())

    def get(self, user_id: str) -> User:
        """
        Get a user by ID.

        Args:
            user_id: User ID (UUID)

        Returns:
            User object
        """
        response = self._http.get(f"/users/{user_id}")
        response.raise_for_status()
        return User(**response.json())

    def create(self, request: CreateUserRequest) -> User:
        """
        Create a new user.

        Args:
            request: User creation request

        Returns:
            Created user
        """
        response = self._http.post("/users", json=request.model_dump(exclude_none=True))
        response.raise_for_status()
        return User(**response.json())

    def update(self, user_id: str, request: UpdateUserRequest) -> User:
        """
        Update a user.

        Args:
            user_id: User ID
            request: Update request

        Returns:
            Updated user
        """
        response = self._http.put(
            f"/users/{user_id}",
            json=request.model_dump(exclude_none=True),
        )
        response.raise_for_status()
        return User(**response.json())

    def delete(self, user_id: str) -> None:
        """
        Delete a user (soft delete).

        Args:
            user_id: User ID
        """
        response = self._http.delete(f"/users/{user_id}")
        response.raise_for_status()

    def update_status(self, user_id: str, status: str, reason: Optional[str] = None) -> User:
        """
        Update a user's status.

        Args:
            user_id: User ID
            status: New status (active, inactive, suspended)
            reason: Reason for status change

        Returns:
            Updated user
        """
        data = {"status": status}
        if reason:
            data["reason"] = reason

        response = self._http.patch(f"/users/{user_id}/status", json=data)
        response.raise_for_status()
        return User(**response.json())

    def update_roles(self, user_id: str, roles: list[str]) -> User:
        """
        Update a user's roles.

        Args:
            user_id: User ID
            roles: New roles

        Returns:
            Updated user
        """
        response = self._http.put(f"/users/{user_id}/roles", json={"roles": roles})
        response.raise_for_status()
        return User(**response.json())

    def reset_password(self, user_id: str, send_email: bool = True) -> dict:
        """
        Reset a user's password.

        Args:
            user_id: User ID
            send_email: Whether to send reset email

        Returns:
            Response with message and optional temporary password
        """
        response = self._http.post(
            f"/users/{user_id}/password",
            json={"send_email": send_email},
        )
        response.raise_for_status()
        return response.json()

    def invite(self, request: InviteUserRequest) -> dict:
        """
        Invite a new user.

        Args:
            request: Invitation request

        Returns:
            Invitation details
        """
        response = self._http.post(
            "/users/invite",
            json=request.model_dump(exclude_none=True),
        )
        response.raise_for_status()
        return response.json()

    def get_stats(self) -> UserStats:
        """
        Get user statistics for the tenant.

        Returns:
            UserStats with aggregated data
        """
        response = self._http.get("/users/stats")
        response.raise_for_status()
        return UserStats(**response.json())

    # Profile operations (for current user)

    def get_my_profile(self) -> UserProfile:
        """Get current user's profile."""
        response = self._http.get("/me")
        response.raise_for_status()
        return UserProfile(**response.json())

    def update_my_profile(self, **kwargs) -> UserProfile:
        """Update current user's profile."""
        response = self._http.patch("/me", json=kwargs)
        response.raise_for_status()
        return UserProfile(**response.json())

    def get_my_preferences(self) -> UserPreferences:
        """Get current user's preferences."""
        response = self._http.get("/me/preferences")
        response.raise_for_status()
        return UserPreferences(**response.json())

    def update_my_preferences(self, **kwargs) -> UserPreferences:
        """Update current user's preferences."""
        response = self._http.patch("/me/preferences", json=kwargs)
        response.raise_for_status()
        return UserPreferences(**response.json())

    def change_password(self, current_password: str, new_password: str) -> None:
        """
        Change current user's password.

        Args:
            current_password: Current password
            new_password: New password
        """
        response = self._http.post(
            "/me/password",
            json={"current_password": current_password, "new_password": new_password},
        )
        response.raise_for_status()

    def close(self) -> None:
        """Close the HTTP client."""
        self._http.close()

    def __enter__(self) -> "UserClient":
        return self

    def __exit__(self, *args) -> None:
        self.close()
