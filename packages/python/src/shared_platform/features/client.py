"""Feature flag client for managing and evaluating feature toggles."""

from typing import Any, Optional

import httpx

from .models import (
    FeatureFlag,
    FeatureFlagListResponse,
    FeatureFlagEvaluation,
    EvaluationContext,
)


class FeatureFlagClient:
    """Client for feature flag operations."""

    def __init__(
        self,
        base_url: str,
        access_token: Optional[str] = None,
        timeout: float = 30.0,
        cache_ttl: int = 60,
    ):
        """
        Initialize the feature flag client.

        Args:
            base_url: Base URL of the API server
            access_token: Optional access token for authentication
            timeout: Request timeout in seconds
            cache_ttl: Cache TTL in seconds for flag values
        """
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.cache_ttl = cache_ttl
        self._access_token = access_token
        self._cache: dict[str, tuple[Any, float]] = {}

    def set_access_token(self, token: str) -> None:
        """Set the access token for authenticated requests."""
        self._access_token = token

    def _get_headers(self) -> dict[str, str]:
        """Get request headers including auth token."""
        headers = {"Content-Type": "application/json"}
        if self._access_token:
            headers["Authorization"] = f"Bearer {self._access_token}"
        return headers

    async def list(
        self,
        page: int = 1,
        page_size: int = 50,
        tag: Optional[str] = None,
        enabled: Optional[bool] = None,
    ) -> FeatureFlagListResponse:
        """
        List feature flags.

        Args:
            page: Page number (1-indexed)
            page_size: Number of items per page
            tag: Filter by tag
            enabled: Filter by enabled status

        Returns:
            List response with feature flags and pagination
        """
        params: dict[str, str | int | bool] = {
            "page": page,
            "page_size": page_size,
        }
        if tag:
            params["tag"] = tag
        if enabled is not None:
            params["enabled"] = enabled

        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.get(
                f"{self.base_url}/api/features",
                params=params,
                headers=self._get_headers(),
            )
            response.raise_for_status()
            return FeatureFlagListResponse(**response.json())

    async def get(self, key: str) -> FeatureFlag:
        """
        Get a feature flag by key.

        Args:
            key: The feature flag key

        Returns:
            The feature flag
        """
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.get(
                f"{self.base_url}/api/features/{key}",
                headers=self._get_headers(),
            )
            response.raise_for_status()
            return FeatureFlag(**response.json())

    async def evaluate(
        self,
        key: str,
        context: Optional[EvaluationContext] = None,
        default: Any = False,
    ) -> FeatureFlagEvaluation:
        """
        Evaluate a feature flag for a given context.

        Args:
            key: The feature flag key
            context: Optional evaluation context
            default: Default value if flag not found

        Returns:
            The evaluation result
        """
        data = {}
        if context:
            data = context.model_dump(exclude_none=True)

        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.post(
                f"{self.base_url}/api/features/{key}/evaluate",
                json=data,
                headers=self._get_headers(),
            )

            if response.status_code == 404:
                return FeatureFlagEvaluation(
                    key=key,
                    enabled=default if isinstance(default, bool) else False,
                    value=default,
                    reason="flag_not_found",
                )

            response.raise_for_status()
            return FeatureFlagEvaluation(**response.json())

    async def is_enabled(
        self,
        key: str,
        context: Optional[EvaluationContext] = None,
        default: bool = False,
    ) -> bool:
        """
        Check if a feature flag is enabled for a given context.

        Args:
            key: The feature flag key
            context: Optional evaluation context
            default: Default value if flag not found

        Returns:
            Whether the flag is enabled
        """
        result = await self.evaluate(key, context, default)
        return result.enabled

    async def get_value(
        self,
        key: str,
        context: Optional[EvaluationContext] = None,
        default: Any = None,
    ) -> Any:
        """
        Get the value of a feature flag for a given context.

        Args:
            key: The feature flag key
            context: Optional evaluation context
            default: Default value if flag not found

        Returns:
            The flag value
        """
        result = await self.evaluate(key, context, default)
        return result.value

    async def evaluate_all(
        self,
        context: Optional[EvaluationContext] = None,
    ) -> dict[str, FeatureFlagEvaluation]:
        """
        Evaluate all feature flags for a given context.

        Args:
            context: Optional evaluation context

        Returns:
            Dictionary of flag key to evaluation result
        """
        data = {}
        if context:
            data = context.model_dump(exclude_none=True)

        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.post(
                f"{self.base_url}/api/features/evaluate-all",
                json=data,
                headers=self._get_headers(),
            )
            response.raise_for_status()

            result = {}
            for key, value in response.json().items():
                result[key] = FeatureFlagEvaluation(**value)
            return result

    def create_context(
        self,
        user_id: Optional[str] = None,
        email: Optional[str] = None,
        tenant_id: Optional[str] = None,
        roles: Optional[list[str]] = None,
        **attributes: Any,
    ) -> EvaluationContext:
        """
        Create an evaluation context.

        Args:
            user_id: User ID
            email: User email
            tenant_id: Tenant ID
            roles: User roles
            **attributes: Additional attributes

        Returns:
            Evaluation context
        """
        return EvaluationContext(
            user_id=user_id,
            email=email,
            tenant_id=tenant_id,
            roles=roles or [],
            attributes=attributes,
        )
