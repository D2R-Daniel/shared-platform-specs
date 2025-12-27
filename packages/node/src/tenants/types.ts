/**
 * Tenant and organization types for multi-tenant support.
 */

// Enums as union types
export type TenantStatus = 'active' | 'inactive' | 'suspended' | 'pending';
export type SubscriptionPlan = 'free' | 'basic' | 'pro' | 'enterprise';
export type BillingCycle = 'monthly' | 'annual';
export type SSOProvider = 'azure_ad' | 'okta' | 'google' | 'saml' | 'oidc';
export type SyncFrequency = 'manual' | 'hourly' | 'daily' | 'weekly';

// Contact and Address

export interface PrimaryContact {
  name?: string;
  email?: string;
  phone?: string;
}

export interface Address {
  street?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
}

// Tenant Features

export interface TenantFeatures {
  ssoEnabled?: boolean;
  scimEnabled?: boolean;
  customBrandingEnabled?: boolean;
  analyticsEnabled?: boolean;
  maxUsers?: number;
  maxStorageGb?: number;
}

// SSO Configuration Models

export interface AzureADConfig {
  tenantId?: string;
  clientId?: string;
  clientSecretEncrypted?: string;
  discoveryUrl?: string;
}

export interface OktaConfig {
  domain?: string;
  clientId?: string;
  clientSecretEncrypted?: string;
  authorizationServer?: string;
}

export interface GoogleConfig {
  clientId?: string;
  clientSecretEncrypted?: string;
  hostedDomain?: string;
}

export interface SAMLConfig {
  metadataUrl?: string;
  entityId?: string;
  ssoUrl?: string;
  sloUrl?: string;
  certificate?: string;
  signatureAlgorithm?: string;
  nameIdFormat?: string;
}

export interface OIDCConfig {
  issuer?: string;
  clientId?: string;
  clientSecretEncrypted?: string;
  authorizationEndpoint?: string;
  tokenEndpoint?: string;
  userinfoEndpoint?: string;
  jwksUri?: string;
  scopes?: string[];
}

export interface SCIMConfig {
  enabled?: boolean;
  bearerToken?: string;
  baseUrl?: string;
  syncGroups?: boolean;
}

export interface JITProvisioningConfig {
  enabled?: boolean;
  defaultRoleId?: string;
  defaultDepartmentId?: string;
  requireEmailDomain?: boolean;
}

export interface AttributeMappings {
  userId?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  displayName?: string;
  groups?: string;
  department?: string;
  jobTitle?: string;
  phone?: string;
  avatarUrl?: string;
  [key: string]: string | undefined;
}

// Main Models

export interface SSOConfig {
  id?: string;
  tenantId: string;
  provider: SSOProvider;
  enabled: boolean;
  displayName?: string;

  // Provider-specific configs
  azureAd?: AzureADConfig;
  okta?: OktaConfig;
  google?: GoogleConfig;
  saml?: SAMLConfig;
  oidc?: OIDCConfig;

  // SCIM and JIT
  scim?: SCIMConfig;
  jitProvisioning?: JITProvisioningConfig;

  // Attribute mappings
  attributeMappings?: AttributeMappings;

  // Sync settings
  lastSyncAt?: string;
  syncFrequency?: SyncFrequency;

  // Timestamps
  createdAt?: string;
  updatedAt?: string;
}

export interface Tenant {
  id?: string;
  name: string;
  slug: string;
  domain?: string;
  logoUrl?: string;
  primaryColor?: string;

  // Status
  status: TenantStatus;
  statusReason?: string;

  // Subscription
  plan: SubscriptionPlan;
  billingCycle?: BillingCycle;
  contractEndDate?: string;

  // Contact info
  primaryContact?: PrimaryContact;
  address?: Address;

  // Company info
  industry?: string;
  companySize?: string;

  // Features
  features?: TenantFeatures;

  // Extensible fields
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;

  // Timestamps
  createdAt?: string;
  updatedAt?: string;
  deletedAt?: string;

  // Audit
  createdBy?: string;
  updatedBy?: string;
}

export interface TenantSummary {
  id: string;
  name: string;
  slug: string;
  status: TenantStatus;
  plan: SubscriptionPlan;
  userCount?: number;
  createdAt?: string;
}

export interface Department {
  id?: string;
  tenantId: string;
  name: string;
  code?: string;
  description?: string;

  // Hierarchy
  parentId?: string;
  path?: string;
  level: number;

  // Management
  headUserId?: string;

  // Status
  isActive: boolean;
  sortOrder?: number;

  // Optional fields
  locationId?: string;
  costCenter?: string;

  // Extensible
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;

  // Timestamps
  createdAt?: string;
  updatedAt?: string;
  deletedAt?: string;

  // Audit
  createdBy?: string;
  updatedBy?: string;
}

export interface DepartmentSummary {
  id: string;
  name: string;
  code?: string;
  level: number;
  isActive: boolean;
  memberCount?: number;
}

export interface DepartmentTree extends Department {
  children: DepartmentTree[];
  memberCount?: number;
  totalMemberCount?: number;
}

export interface UserSummary {
  id: string;
  email: string;
  name?: string;
  picture?: string;
}

export interface DepartmentWithDetails extends Department {
  head?: UserSummary;
  parent?: DepartmentSummary;
  childrenCount?: number;
  memberCount?: number;
}

// Request/Response Models

export interface CreateTenantRequest {
  name: string;
  slug: string;
  domain?: string;
  logoUrl?: string;
  primaryColor?: string;
  plan?: SubscriptionPlan;
  primaryContact?: PrimaryContact;
  industry?: string;
  companySize?: string;
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;
}

export interface UpdateTenantRequest {
  name?: string;
  domain?: string;
  logoUrl?: string;
  primaryColor?: string;
  plan?: SubscriptionPlan;
  billingCycle?: BillingCycle;
  primaryContact?: PrimaryContact;
  industry?: string;
  companySize?: string;
  features?: TenantFeatures;
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;
}

export interface UpdateSSOConfigRequest {
  provider: SSOProvider;
  enabled?: boolean;
  displayName?: string;
  azureAd?: AzureADConfig & { clientSecret?: string };
  okta?: OktaConfig & { clientSecret?: string };
  google?: GoogleConfig & { clientSecret?: string };
  saml?: SAMLConfig;
  oidc?: OIDCConfig & { clientSecret?: string };
  scim?: SCIMConfig;
  jitProvisioning?: JITProvisioningConfig;
  attributeMappings?: AttributeMappings;
  syncFrequency?: SyncFrequency;
}

export interface CreateDepartmentRequest {
  name: string;
  code?: string;
  description?: string;
  parentId?: string;
  headUserId?: string;
  isActive?: boolean;
  sortOrder?: number;
  locationId?: string;
  costCenter?: string;
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;
}

export interface UpdateDepartmentRequest {
  name?: string;
  description?: string;
  headUserId?: string;
  isActive?: boolean;
  sortOrder?: number;
  locationId?: string;
  costCenter?: string;
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;
}

export interface Pagination {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

export interface TenantListResponse {
  data: TenantSummary[];
  pagination: Pagination;
}

export interface DepartmentListResponse {
  data: DepartmentSummary[];
  pagination: Pagination;
}

export interface DepartmentMembersResponse {
  data: UserSummary[];
  pagination: Pagination;
}

export interface SSOTestResult {
  success: boolean;
  message: string;
  details?: Record<string, unknown>;
}

export interface SSOSyncResult {
  syncId: string;
  status: string;
  startedAt: string;
}

// List parameters

export interface ListTenantsParams {
  page?: number;
  pageSize?: number;
  status?: TenantStatus;
  plan?: SubscriptionPlan;
  search?: string;
  sort?: string;
}

export interface ListDepartmentsParams {
  page?: number;
  pageSize?: number;
  parentId?: string;
  isActive?: boolean;
  search?: string;
  includeChildren?: boolean;
  sort?: string;
}

export interface GetDepartmentTreeParams {
  rootId?: string;
  maxDepth?: number;
  includeMembers?: boolean;
}

export interface ListDepartmentMembersParams {
  page?: number;
  pageSize?: number;
  includeSubdepartments?: boolean;
  status?: 'active' | 'inactive' | 'all';
}
