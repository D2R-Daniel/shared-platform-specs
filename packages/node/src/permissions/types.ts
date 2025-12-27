/**
 * Permission and role types for RBAC.
 */

// Enums as union types
export type RoleStatus = 'active' | 'inactive';

// Permission format: "resource:action"
export interface Permission {
  resource: string;
  action: string;
  description?: string;
}

// Role models

export interface Role {
  id?: string;
  tenantId?: string;
  name: string;
  slug: string;
  description?: string;
  permissions: string[];
  hierarchyLevel: number;
  isSystem: boolean;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface RoleSummary {
  id: string;
  name: string;
  slug: string;
  description?: string;
  hierarchyLevel: number;
  isSystem: boolean;
  isActive: boolean;
  permissionCount: number;
}

export interface RoleAssignment {
  id?: string;
  userId: string;
  roleId: string;
  grantedAt?: string;
  grantedBy?: string;
  expiresAt?: string;
}

export interface UserRole {
  roleId: string;
  roleName: string;
  roleSlug: string;
  grantedAt?: string;
  expiresAt?: string;
}

export interface PermissionCheckResult {
  allowed: boolean;
  matchedPermission?: string;
  matchedRole?: string;
  reason?: string;
}

// Request types

export interface CreateRoleRequest {
  name: string;
  slug: string;
  description?: string;
  permissions: string[];
  hierarchyLevel?: number;
  isActive?: boolean;
}

export interface UpdateRoleRequest {
  name?: string;
  description?: string;
  permissions?: string[];
  hierarchyLevel?: number;
  isActive?: boolean;
}

export interface AssignRoleRequest {
  roleId: string;
  expiresAt?: string;
}

export interface PermissionCheckRequest {
  userId: string;
  permission: string;
  resourceId?: string;
}

// Pagination

export interface Pagination {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

export interface RoleListResponse {
  data: RoleSummary[];
  pagination: Pagination;
}

export interface UserRolesResponse {
  data: UserRole[];
  userId: string;
}

// List parameters

export interface ListRolesParams {
  page?: number;
  pageSize?: number;
  isActive?: boolean;
  isSystem?: boolean;
  search?: string;
  sort?: string;
}
