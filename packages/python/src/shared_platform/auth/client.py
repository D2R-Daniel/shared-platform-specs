"""
Authentication client for OAuth2/OIDC operations.
"""

from __future__ import annotations

from typing import Optional
from datetime import datetime, timedelta
import httpx
import jwt

from shared_platform.auth.models import (
    TokenResponse,
    TokenIntrospection,
    UserInfo,
    UserContext,
    Session,
)
from shared_platform.auth.exceptions import (
    AuthError,
    TokenExpiredError,
    InvalidTokenError,
    UnauthorizedError,
)


class AuthClient:
    """
    Client for authentication operations.

    Usage:
        auth = AuthClient(
            issuer_url="https://auth.example.com",
            client_id="your-client-id",
            client_secret="your-client-secret",  # Optional
        )

        # Get tokens
        tokens = auth.login(username="user@example.com", password="password")

        # Refresh token
        new_tokens = auth.refresh_token(tokens.refresh_token)

        # Get user context from token
        context = auth.get_user_context(tokens.access_token)

        # Validate token
        introspection = auth.introspect_token(tokens.access_token)
    """

    def __init__(
        self,
        issuer_url: str,
        client_id: Optional[str] = None,
        client_secret: Optional[str] = None,
        timeout: float = 30.0,
    ):
        """
        Initialize the auth client.

        Args:
            issuer_url: Base URL of the authentication server
            client_id: OAuth2 client ID
            client_secret: OAuth2 client secret (for confidential clients)
            timeout: Request timeout in seconds
        """
        self.issuer_url = issuer_url.rstrip("/")
        self.client_id = client_id
        self.client_secret = client_secret
        self._http = httpx.Client(
            base_url=f"{self.issuer_url}/auth",
            timeout=timeout,
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )
        self._jwks: Optional[dict] = None
        self._jwks_expires_at: Optional[datetime] = None

    def login(
        self,
        username: str,
        password: str,
        scope: str = "openid profile email",
    ) -> TokenResponse:
        """
        Authenticate with username and password (Resource Owner Password flow).

        Args:
            username: User's email or username
            password: User's password
            scope: OAuth2 scopes to request

        Returns:
            TokenResponse with access_token, refresh_token, etc.

        Raises:
            AuthError: If authentication fails
        """
        data = {
            "grant_type": "password",
            "username": username,
            "password": password,
            "scope": scope,
        }
        if self.client_id:
            data["client_id"] = self.client_id
        if self.client_secret:
            data["client_secret"] = self.client_secret

        response = self._http.post("/token", data=data)

        if response.status_code == 401:
            raise UnauthorizedError("Invalid credentials")
        if response.status_code != 200:
            error_data = response.json()
            raise AuthError(
                error=error_data.get("error", "unknown_error"),
                description=error_data.get("error_description", "Authentication failed"),
            )

        return TokenResponse(**response.json())

    def refresh_token(self, refresh_token: str) -> TokenResponse:
        """
        Exchange a refresh token for a new access token.

        Args:
            refresh_token: Valid refresh token

        Returns:
            TokenResponse with new access_token

        Raises:
            TokenExpiredError: If refresh token is expired
            AuthError: If refresh fails
        """
        data = {
            "grant_type": "refresh_token",
            "refresh_token": refresh_token,
        }
        if self.client_id:
            data["client_id"] = self.client_id
        if self.client_secret:
            data["client_secret"] = self.client_secret

        response = self._http.post("/token/refresh", json={"refresh_token": refresh_token})

        if response.status_code == 401:
            raise TokenExpiredError("Refresh token is expired or invalid")
        if response.status_code != 200:
            error_data = response.json()
            raise AuthError(
                error=error_data.get("error", "unknown_error"),
                description=error_data.get("error_description", "Token refresh failed"),
            )

        return TokenResponse(**response.json())

    def revoke_token(self, token: str, token_type_hint: str = "access_token") -> None:
        """
        Revoke an access or refresh token.

        Args:
            token: Token to revoke
            token_type_hint: Type of token ("access_token" or "refresh_token")
        """
        self._http.post(
            "/token/revoke",
            json={"token": token, "token_type_hint": token_type_hint},
            headers={"Authorization": f"Bearer {token}"},
        )

    def introspect_token(self, token: str) -> TokenIntrospection:
        """
        Validate and introspect a token.

        Args:
            token: Token to introspect

        Returns:
            TokenIntrospection with token claims and validity
        """
        response = self._http.post(
            "/token/introspect",
            json={"token": token},
            headers={"Authorization": f"Bearer {token}"},
        )
        return TokenIntrospection(**response.json())

    def get_user_info(self, access_token: str) -> UserInfo:
        """
        Get user information from the OIDC userinfo endpoint.

        Args:
            access_token: Valid access token

        Returns:
            UserInfo with user profile data
        """
        response = self._http.get(
            "/userinfo",
            headers={"Authorization": f"Bearer {access_token}"},
        )

        if response.status_code == 401:
            raise InvalidTokenError("Access token is invalid or expired")

        return UserInfo(**response.json())

    def get_user_context(self, access_token: str) -> UserContext:
        """
        Extract user context from an access token.

        This decodes the JWT and returns a UserContext object
        with user ID, roles, permissions, etc.

        Args:
            access_token: JWT access token

        Returns:
            UserContext with authenticated user information

        Raises:
            InvalidTokenError: If token is malformed
            TokenExpiredError: If token is expired
        """
        try:
            # Decode without verification for extracting claims
            # In production, you should verify the signature
            claims = jwt.decode(
                access_token,
                options={"verify_signature": False},
            )
        except jwt.ExpiredSignatureError:
            raise TokenExpiredError("Access token is expired")
        except jwt.DecodeError as e:
            raise InvalidTokenError(f"Invalid token format: {e}")

        return UserContext(
            user_id=claims.get("sub"),
            email=claims.get("email"),
            email_verified=claims.get("email_verified", False),
            name=claims.get("name"),
            given_name=claims.get("given_name"),
            family_name=claims.get("family_name"),
            picture=claims.get("picture"),
            roles=claims.get("roles", []),
            permissions=claims.get("permissions", []),
            tenant_id=claims.get("tenant_id"),
            session_id=claims.get("session_id"),
            scopes=claims.get("scope", "").split() if claims.get("scope") else [],
            is_authenticated=True,
        )

    def logout(self, access_token: str) -> None:
        """
        Logout the current user and revoke all tokens.

        Args:
            access_token: Current access token
        """
        self._http.post(
            "/logout",
            headers={"Authorization": f"Bearer {access_token}"},
        )

    def list_sessions(self, access_token: str) -> list[Session]:
        """
        List all active sessions for the current user.

        Args:
            access_token: Current access token

        Returns:
            List of active sessions
        """
        response = self._http.get(
            "/sessions",
            headers={"Authorization": f"Bearer {access_token}"},
        )
        data = response.json()
        return [Session(**s) for s in data.get("sessions", [])]

    def terminate_session(self, access_token: str, session_id: str) -> None:
        """
        Terminate a specific session.

        Args:
            access_token: Current access token
            session_id: ID of session to terminate
        """
        self._http.delete(
            f"/sessions/{session_id}",
            headers={"Authorization": f"Bearer {access_token}"},
        )

    def close(self) -> None:
        """Close the HTTP client."""
        self._http.close()

    def __enter__(self) -> "AuthClient":
        return self

    def __exit__(self, *args) -> None:
        self.close()
