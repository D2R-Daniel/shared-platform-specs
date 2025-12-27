"""
Tenant and organization models for multi-tenant support.
"""

from datetime import datetime
from enum import Enum
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class TenantStatus(str, Enum):
    """Tenant account status."""
    ACTIVE = "active"
    INACTIVE = "inactive"
    SUSPENDED = "suspended"
    PENDING = "pending"


class SubscriptionPlan(str, Enum):
    """Subscription plan levels."""
    FREE = "free"
    BASIC = "basic"
    PRO = "pro"
    ENTERPRISE = "enterprise"


class BillingCycle(str, Enum):
    """Billing frequency."""
    MONTHLY = "monthly"
    ANNUAL = "annual"


class SSOProvider(str, Enum):
    """SSO identity provider types."""
    AZURE_AD = "azure_ad"
    OKTA = "okta"
    GOOGLE = "google"
    SAML = "saml"
    OIDC = "oidc"


class SyncFrequency(str, Enum):
    """Directory sync frequency."""
    MANUAL = "manual"
    HOURLY = "hourly"
    DAILY = "daily"
    WEEKLY = "weekly"


# Contact and Address Models

class PrimaryContact(BaseModel):
    """Primary contact for the tenant."""
    name: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None


class Address(BaseModel):
    """Physical address."""
    street: Optional[str] = None
    city: Optional[str] = None
    state: Optional[str] = None
    country: Optional[str] = None
    postal_code: Optional[str] = None


# Tenant Features

class TenantFeatures(BaseModel):
    """Feature flags and limits for a tenant."""
    sso_enabled: bool = False
    scim_enabled: bool = False
    custom_branding_enabled: bool = False
    analytics_enabled: bool = True
    max_users: Optional[int] = Field(None, description="0 = unlimited")
    max_storage_gb: Optional[int] = Field(None, description="0 = unlimited")


# SSO Configuration Models

class AzureADConfig(BaseModel):
    """Microsoft Entra ID (Azure AD) configuration."""
    tenant_id: Optional[str] = Field(None, description="Azure AD Tenant ID")
    client_id: Optional[str] = Field(None, description="Application Client ID")
    client_secret_encrypted: Optional[str] = Field(None, exclude=True)
    discovery_url: Optional[str] = None


class OktaConfig(BaseModel):
    """Okta configuration."""
    domain: Optional[str] = Field(None, description="Okta domain")
    client_id: Optional[str] = None
    client_secret_encrypted: Optional[str] = Field(None, exclude=True)
    authorization_server: Optional[str] = "default"


class GoogleConfig(BaseModel):
    """Google Workspace configuration."""
    client_id: Optional[str] = None
    client_secret_encrypted: Optional[str] = Field(None, exclude=True)
    hosted_domain: Optional[str] = Field(None, description="Restrict to domain")


class SAMLConfig(BaseModel):
    """Generic SAML 2.0 configuration."""
    metadata_url: Optional[str] = None
    entity_id: Optional[str] = None
    sso_url: Optional[str] = None
    slo_url: Optional[str] = None
    certificate: Optional[str] = None
    signature_algorithm: str = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
    name_id_format: str = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress"


class OIDCConfig(BaseModel):
    """Generic OpenID Connect configuration."""
    issuer: Optional[str] = None
    client_id: Optional[str] = None
    client_secret_encrypted: Optional[str] = Field(None, exclude=True)
    authorization_endpoint: Optional[str] = None
    token_endpoint: Optional[str] = None
    userinfo_endpoint: Optional[str] = None
    jwks_uri: Optional[str] = None
    scopes: List[str] = Field(default_factory=lambda: ["openid", "profile", "email"])


class SCIMConfig(BaseModel):
    """SCIM 2.0 provisioning configuration."""
    enabled: bool = False
    bearer_token: Optional[str] = Field(None, exclude=True)
    base_url: Optional[str] = None
    sync_groups: bool = True


class JITProvisioningConfig(BaseModel):
    """Just-In-Time provisioning configuration."""
    enabled: bool = True
    default_role_id: Optional[str] = None
    default_department_id: Optional[str] = None
    require_email_domain: bool = False


class AttributeMappings(BaseModel):
    """Map IdP attributes to platform user fields."""
    user_id: str = "sub"
    email: str = "email"
    first_name: str = "given_name"
    last_name: str = "family_name"
    display_name: str = "name"
    groups: Optional[str] = None
    department: Optional[str] = None
    job_title: Optional[str] = None
    phone: Optional[str] = None
    avatar_url: Optional[str] = "picture"

    class Config:
        extra = "allow"  # Allow additional custom mappings


# Main Models

class SSOConfig(BaseModel):
    """Single Sign-On configuration for a tenant."""
    id: Optional[str] = None
    tenant_id: str
    provider: SSOProvider
    enabled: bool = False
    display_name: Optional[str] = None

    # Provider-specific configs
    azure_ad: Optional[AzureADConfig] = None
    okta: Optional[OktaConfig] = None
    google: Optional[GoogleConfig] = None
    saml: Optional[SAMLConfig] = None
    oidc: Optional[OIDCConfig] = None

    # SCIM and JIT
    scim: Optional[SCIMConfig] = None
    jit_provisioning: Optional[JITProvisioningConfig] = None

    # Attribute mappings
    attribute_mappings: Optional[AttributeMappings] = None

    # Sync settings
    last_sync_at: Optional[datetime] = None
    sync_frequency: SyncFrequency = SyncFrequency.DAILY

    # Timestamps
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class Tenant(BaseModel):
    """Tenant/Organization model."""
    id: Optional[str] = None
    name: str
    slug: str
    domain: Optional[str] = None
    logo_url: Optional[str] = None
    primary_color: Optional[str] = None

    # Status
    status: TenantStatus = TenantStatus.PENDING
    status_reason: Optional[str] = None

    # Subscription
    plan: SubscriptionPlan = SubscriptionPlan.BASIC
    billing_cycle: BillingCycle = BillingCycle.MONTHLY
    contract_end_date: Optional[datetime] = None

    # Contact info
    primary_contact: Optional[PrimaryContact] = None
    address: Optional[Address] = None

    # Company info
    industry: Optional[str] = None
    company_size: Optional[str] = None

    # Features
    features: Optional[TenantFeatures] = None

    # Extensible fields
    settings: Dict[str, Any] = Field(default_factory=dict)
    metadata: Dict[str, Any] = Field(default_factory=dict)

    # Timestamps
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    deleted_at: Optional[datetime] = None

    # Audit
    created_by: Optional[str] = None
    updated_by: Optional[str] = None


class TenantSummary(BaseModel):
    """Minimal tenant info for lists."""
    id: str
    name: str
    slug: str
    status: TenantStatus
    plan: SubscriptionPlan
    user_count: Optional[int] = None
    created_at: Optional[datetime] = None


class Department(BaseModel):
    """Department model with hierarchy support."""
    id: Optional[str] = None
    tenant_id: str
    name: str
    code: Optional[str] = None
    description: Optional[str] = None

    # Hierarchy
    parent_id: Optional[str] = None
    path: Optional[str] = None
    level: int = 0

    # Management
    head_user_id: Optional[str] = None

    # Status
    is_active: bool = True
    sort_order: int = 0

    # Optional fields
    location_id: Optional[str] = None
    cost_center: Optional[str] = None

    # Extensible
    settings: Dict[str, Any] = Field(default_factory=dict)
    metadata: Dict[str, Any] = Field(default_factory=dict)

    # Timestamps
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    deleted_at: Optional[datetime] = None

    # Audit
    created_by: Optional[str] = None
    updated_by: Optional[str] = None


class DepartmentSummary(BaseModel):
    """Minimal department info for lists and references."""
    id: str
    name: str
    code: Optional[str] = None
    level: int = 0
    is_active: bool = True
    member_count: Optional[int] = None


class DepartmentTree(Department):
    """Department with nested children for tree view."""
    children: List["DepartmentTree"] = Field(default_factory=list)
    member_count: Optional[int] = None
    total_member_count: Optional[int] = None


class UserSummary(BaseModel):
    """Minimal user info for references."""
    id: str
    email: str
    name: Optional[str] = None
    picture: Optional[str] = None


class DepartmentWithDetails(Department):
    """Department with related details."""
    head: Optional[UserSummary] = None
    parent: Optional[DepartmentSummary] = None
    children_count: Optional[int] = None
    member_count: Optional[int] = None


# Request/Response Models

class CreateTenantRequest(BaseModel):
    """Request to create a new tenant."""
    name: str
    slug: str
    domain: Optional[str] = None
    logo_url: Optional[str] = None
    primary_color: Optional[str] = None
    plan: SubscriptionPlan = SubscriptionPlan.BASIC
    primary_contact: Optional[PrimaryContact] = None
    industry: Optional[str] = None
    company_size: Optional[str] = None
    settings: Dict[str, Any] = Field(default_factory=dict)
    metadata: Dict[str, Any] = Field(default_factory=dict)


class UpdateTenantRequest(BaseModel):
    """Request to update a tenant."""
    name: Optional[str] = None
    domain: Optional[str] = None
    logo_url: Optional[str] = None
    primary_color: Optional[str] = None
    plan: Optional[SubscriptionPlan] = None
    billing_cycle: Optional[BillingCycle] = None
    primary_contact: Optional[PrimaryContact] = None
    industry: Optional[str] = None
    company_size: Optional[str] = None
    features: Optional[TenantFeatures] = None
    settings: Optional[Dict[str, Any]] = None
    metadata: Optional[Dict[str, Any]] = None


class UpdateSSOConfigRequest(BaseModel):
    """Request to update SSO configuration."""
    provider: SSOProvider
    enabled: Optional[bool] = None
    display_name: Optional[str] = None
    azure_ad: Optional[AzureADConfig] = None
    okta: Optional[OktaConfig] = None
    google: Optional[GoogleConfig] = None
    saml: Optional[SAMLConfig] = None
    oidc: Optional[OIDCConfig] = None
    scim: Optional[SCIMConfig] = None
    jit_provisioning: Optional[JITProvisioningConfig] = None
    attribute_mappings: Optional[AttributeMappings] = None
    sync_frequency: Optional[SyncFrequency] = None


class CreateDepartmentRequest(BaseModel):
    """Request to create a department."""
    name: str
    code: Optional[str] = None
    description: Optional[str] = None
    parent_id: Optional[str] = None
    head_user_id: Optional[str] = None
    is_active: bool = True
    sort_order: int = 0
    location_id: Optional[str] = None
    cost_center: Optional[str] = None
    settings: Dict[str, Any] = Field(default_factory=dict)
    metadata: Dict[str, Any] = Field(default_factory=dict)


class UpdateDepartmentRequest(BaseModel):
    """Request to update a department."""
    name: Optional[str] = None
    description: Optional[str] = None
    head_user_id: Optional[str] = None
    is_active: Optional[bool] = None
    sort_order: Optional[int] = None
    location_id: Optional[str] = None
    cost_center: Optional[str] = None
    settings: Optional[Dict[str, Any]] = None
    metadata: Optional[Dict[str, Any]] = None


class Pagination(BaseModel):
    """Pagination metadata."""
    page: int = 1
    page_size: int = 20
    total_items: int = 0
    total_pages: int = 0


class TenantListResponse(BaseModel):
    """Response for tenant list."""
    data: List[TenantSummary]
    pagination: Pagination


class DepartmentListResponse(BaseModel):
    """Response for department list."""
    data: List[DepartmentSummary]
    pagination: Pagination


class DepartmentMembersResponse(BaseModel):
    """Response for department members list."""
    data: List[UserSummary]
    pagination: Pagination


class SSOTestResult(BaseModel):
    """Result of SSO connection test."""
    success: bool
    message: str
    details: Optional[Dict[str, Any]] = None


class SSOSyncResult(BaseModel):
    """Result of SSO sync operation."""
    sync_id: str
    status: str
    started_at: datetime


# Update forward references
DepartmentTree.model_rebuild()
