"""
HTTP clients for tenant and department operations.
"""

from typing import Any, Dict, List, Optional

import httpx

from .exceptions import (
    DepartmentNotFoundError,
    SSOConfigNotFoundError,
    TenantNotFoundError,
)
from .models import (
    CreateDepartmentRequest,
    CreateTenantRequest,
    Department,
    DepartmentListResponse,
    DepartmentMembersResponse,
    DepartmentSummary,
    DepartmentTree,
    DepartmentWithDetails,
    Pagination,
    SSOConfig,
    SSOSyncResult,
    SSOTestResult,
    Tenant,
    TenantListResponse,
    TenantStatus,
    TenantSummary,
    UpdateDepartmentRequest,
    UpdateSSOConfigRequest,
    UpdateTenantRequest,
    UserSummary,
)


class TenantClient:
    """Client for tenant management operations."""

    def __init__(
        self,
        base_url: str,
        access_token: Optional[str] = None,
        timeout: float = 30.0,
    ):
        """
        Initialize the tenant client.

        Args:
            base_url: Base URL of the API (e.g., https://api.example.com/v1)
            access_token: Bearer token for authentication
            timeout: Request timeout in seconds
        """
        self.base_url = base_url.rstrip("/")
        self.access_token = access_token
        self.timeout = timeout
        self._client: Optional[httpx.Client] = None

    def _get_client(self) -> httpx.Client:
        """Get or create HTTP client."""
        if self._client is None:
            headers = {"Content-Type": "application/json"}
            if self.access_token:
                headers["Authorization"] = f"Bearer {self.access_token}"
            self._client = httpx.Client(
                base_url=self.base_url,
                headers=headers,
                timeout=self.timeout,
            )
        return self._client

    def close(self):
        """Close the HTTP client."""
        if self._client:
            self._client.close()
            self._client = None

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()

    def _handle_response(self, response: httpx.Response) -> Dict[str, Any]:
        """Handle API response and raise appropriate exceptions."""
        if response.status_code == 404:
            raise TenantNotFoundError("unknown")
        response.raise_for_status()
        if response.status_code == 204:
            return {}
        return response.json()

    # Tenant Operations

    def list(
        self,
        page: int = 1,
        page_size: int = 20,
        status: Optional[TenantStatus] = None,
        plan: Optional[str] = None,
        search: Optional[str] = None,
        sort: str = "created_at:desc",
    ) -> TenantListResponse:
        """
        List tenants with pagination and filtering.

        Args:
            page: Page number (1-indexed)
            page_size: Items per page
            status: Filter by status
            plan: Filter by plan
            search: Search in name, slug, or domain
            sort: Sort field and direction

        Returns:
            TenantListResponse with data and pagination
        """
        params = {
            "page": page,
            "page_size": page_size,
            "sort": sort,
        }
        if status:
            params["status"] = status.value
        if plan:
            params["plan"] = plan
        if search:
            params["search"] = search

        response = self._get_client().get("/tenants", params=params)
        data = self._handle_response(response)
        return TenantListResponse(**data)

    def get(self, tenant_id: str) -> Tenant:
        """
        Get tenant by ID.

        Args:
            tenant_id: Tenant ID

        Returns:
            Tenant object

        Raises:
            TenantNotFoundError: If tenant not found
        """
        response = self._get_client().get(f"/tenants/{tenant_id}")
        if response.status_code == 404:
            raise TenantNotFoundError(tenant_id)
        data = self._handle_response(response)
        return Tenant(**data)

    def create(self, request: CreateTenantRequest) -> Tenant:
        """
        Create a new tenant.

        Args:
            request: Tenant creation request

        Returns:
            Created tenant
        """
        response = self._get_client().post(
            "/tenants",
            json=request.model_dump(exclude_none=True),
        )
        data = self._handle_response(response)
        return Tenant(**data)

    def update(self, tenant_id: str, request: UpdateTenantRequest) -> Tenant:
        """
        Update a tenant.

        Args:
            tenant_id: Tenant ID
            request: Update request

        Returns:
            Updated tenant
        """
        response = self._get_client().put(
            f"/tenants/{tenant_id}",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 404:
            raise TenantNotFoundError(tenant_id)
        data = self._handle_response(response)
        return Tenant(**data)

    def delete(self, tenant_id: str) -> None:
        """
        Delete a tenant (soft-delete).

        Args:
            tenant_id: Tenant ID
        """
        response = self._get_client().delete(f"/tenants/{tenant_id}")
        if response.status_code == 404:
            raise TenantNotFoundError(tenant_id)
        self._handle_response(response)

    def update_status(
        self,
        tenant_id: str,
        status: TenantStatus,
        reason: Optional[str] = None,
    ) -> Tenant:
        """
        Update tenant status.

        Args:
            tenant_id: Tenant ID
            status: New status
            reason: Reason for status change

        Returns:
            Updated tenant
        """
        payload = {"status": status.value}
        if reason:
            payload["reason"] = reason

        response = self._get_client().patch(
            f"/tenants/{tenant_id}/status",
            json=payload,
        )
        if response.status_code == 404:
            raise TenantNotFoundError(tenant_id)
        data = self._handle_response(response)
        return Tenant(**data)

    # SSO Operations

    def get_sso_config(self, tenant_id: str) -> SSOConfig:
        """
        Get SSO configuration for a tenant.

        Args:
            tenant_id: Tenant ID

        Returns:
            SSO configuration
        """
        response = self._get_client().get(f"/tenants/{tenant_id}/sso")
        if response.status_code == 404:
            raise SSOConfigNotFoundError(tenant_id)
        data = self._handle_response(response)
        return SSOConfig(**data)

    def update_sso_config(
        self,
        tenant_id: str,
        request: UpdateSSOConfigRequest,
    ) -> SSOConfig:
        """
        Update SSO configuration for a tenant.

        Args:
            tenant_id: Tenant ID
            request: SSO configuration update request

        Returns:
            Updated SSO configuration
        """
        response = self._get_client().put(
            f"/tenants/{tenant_id}/sso",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 404:
            raise TenantNotFoundError(tenant_id)
        data = self._handle_response(response)
        return SSOConfig(**data)

    def delete_sso_config(self, tenant_id: str) -> None:
        """
        Delete SSO configuration for a tenant.

        Args:
            tenant_id: Tenant ID
        """
        response = self._get_client().delete(f"/tenants/{tenant_id}/sso")
        if response.status_code == 404:
            raise TenantNotFoundError(tenant_id)
        self._handle_response(response)

    def test_sso_connection(self, tenant_id: str) -> SSOTestResult:
        """
        Test SSO connection for a tenant.

        Args:
            tenant_id: Tenant ID

        Returns:
            Test result
        """
        response = self._get_client().post(f"/tenants/{tenant_id}/sso/test")
        if response.status_code == 404:
            raise TenantNotFoundError(tenant_id)
        data = self._handle_response(response)
        return SSOTestResult(**data)

    def trigger_sso_sync(self, tenant_id: str) -> SSOSyncResult:
        """
        Trigger user sync from IdP.

        Args:
            tenant_id: Tenant ID

        Returns:
            Sync operation result
        """
        response = self._get_client().post(f"/tenants/{tenant_id}/sso/sync")
        if response.status_code == 404:
            raise TenantNotFoundError(tenant_id)
        data = self._handle_response(response)
        return SSOSyncResult(**data)


class DepartmentClient:
    """Client for department management operations."""

    def __init__(
        self,
        base_url: str,
        access_token: Optional[str] = None,
        timeout: float = 30.0,
    ):
        """
        Initialize the department client.

        Args:
            base_url: Base URL of the API
            access_token: Bearer token for authentication
            timeout: Request timeout in seconds
        """
        self.base_url = base_url.rstrip("/")
        self.access_token = access_token
        self.timeout = timeout
        self._client: Optional[httpx.Client] = None

    def _get_client(self) -> httpx.Client:
        """Get or create HTTP client."""
        if self._client is None:
            headers = {"Content-Type": "application/json"}
            if self.access_token:
                headers["Authorization"] = f"Bearer {self.access_token}"
            self._client = httpx.Client(
                base_url=self.base_url,
                headers=headers,
                timeout=self.timeout,
            )
        return self._client

    def close(self):
        """Close the HTTP client."""
        if self._client:
            self._client.close()
            self._client = None

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()

    def _handle_response(self, response: httpx.Response) -> Dict[str, Any]:
        """Handle API response."""
        if response.status_code == 404:
            raise DepartmentNotFoundError("unknown")
        response.raise_for_status()
        if response.status_code == 204:
            return {}
        return response.json()

    # Department Operations

    def list(
        self,
        page: int = 1,
        page_size: int = 20,
        parent_id: Optional[str] = None,
        is_active: Optional[bool] = None,
        search: Optional[str] = None,
        include_children: bool = False,
        sort: str = "name:asc",
    ) -> DepartmentListResponse:
        """
        List departments.

        Args:
            page: Page number
            page_size: Items per page
            parent_id: Filter by parent (use 'root' for top-level)
            is_active: Filter by active status
            search: Search in name or code
            include_children: Include nested children
            sort: Sort field and direction

        Returns:
            DepartmentListResponse
        """
        params = {
            "page": page,
            "page_size": page_size,
            "sort": sort,
            "include_children": include_children,
        }
        if parent_id:
            params["parent_id"] = parent_id
        if is_active is not None:
            params["is_active"] = is_active
        if search:
            params["search"] = search

        response = self._get_client().get("/departments", params=params)
        data = self._handle_response(response)
        return DepartmentListResponse(**data)

    def get_tree(
        self,
        root_id: Optional[str] = None,
        max_depth: int = 10,
        include_members: bool = False,
    ) -> List[DepartmentTree]:
        """
        Get department hierarchy as a tree.

        Args:
            root_id: Start from specific department
            max_depth: Maximum depth to return
            include_members: Include member counts

        Returns:
            List of department trees
        """
        params = {
            "max_depth": max_depth,
            "include_members": include_members,
        }
        if root_id:
            params["root_id"] = root_id

        response = self._get_client().get("/departments/tree", params=params)
        data = self._handle_response(response)
        return [DepartmentTree(**d) for d in data.get("data", [])]

    def get(
        self,
        department_id: str,
        include_head: bool = False,
        include_parent: bool = False,
    ) -> DepartmentWithDetails:
        """
        Get department by ID.

        Args:
            department_id: Department ID
            include_head: Include head user details
            include_parent: Include parent department details

        Returns:
            Department with details
        """
        params = {
            "include_head": include_head,
            "include_parent": include_parent,
        }
        response = self._get_client().get(
            f"/departments/{department_id}",
            params=params,
        )
        if response.status_code == 404:
            raise DepartmentNotFoundError(department_id)
        data = self._handle_response(response)
        return DepartmentWithDetails(**data)

    def create(self, request: CreateDepartmentRequest) -> Department:
        """
        Create a new department.

        Args:
            request: Department creation request

        Returns:
            Created department
        """
        response = self._get_client().post(
            "/departments",
            json=request.model_dump(exclude_none=True),
        )
        data = self._handle_response(response)
        return Department(**data)

    def update(
        self,
        department_id: str,
        request: UpdateDepartmentRequest,
    ) -> Department:
        """
        Update a department.

        Args:
            department_id: Department ID
            request: Update request

        Returns:
            Updated department
        """
        response = self._get_client().put(
            f"/departments/{department_id}",
            json=request.model_dump(exclude_none=True),
        )
        if response.status_code == 404:
            raise DepartmentNotFoundError(department_id)
        data = self._handle_response(response)
        return Department(**data)

    def delete(self, department_id: str, force: bool = False) -> None:
        """
        Delete a department.

        Args:
            department_id: Department ID
            force: Force delete even with members
        """
        params = {"force": force} if force else {}
        response = self._get_client().delete(
            f"/departments/{department_id}",
            params=params,
        )
        if response.status_code == 404:
            raise DepartmentNotFoundError(department_id)
        self._handle_response(response)

    def get_members(
        self,
        department_id: str,
        page: int = 1,
        page_size: int = 20,
        include_subdepartments: bool = False,
        status: str = "active",
    ) -> DepartmentMembersResponse:
        """
        Get members of a department.

        Args:
            department_id: Department ID
            page: Page number
            page_size: Items per page
            include_subdepartments: Include members from child departments
            status: Filter by user status (active, inactive, all)

        Returns:
            DepartmentMembersResponse
        """
        params = {
            "page": page,
            "page_size": page_size,
            "include_subdepartments": include_subdepartments,
            "status": status,
        }
        response = self._get_client().get(
            f"/departments/{department_id}/members",
            params=params,
        )
        if response.status_code == 404:
            raise DepartmentNotFoundError(department_id)
        data = self._handle_response(response)
        return DepartmentMembersResponse(**data)

    def move(
        self,
        department_id: str,
        new_parent_id: Optional[str] = None,
    ) -> Department:
        """
        Move department to a new parent.

        Args:
            department_id: Department ID
            new_parent_id: New parent ID (None for root)

        Returns:
            Updated department
        """
        response = self._get_client().post(
            f"/departments/{department_id}/move",
            json={"new_parent_id": new_parent_id},
        )
        if response.status_code == 404:
            raise DepartmentNotFoundError(department_id)
        data = self._handle_response(response)
        return Department(**data)
