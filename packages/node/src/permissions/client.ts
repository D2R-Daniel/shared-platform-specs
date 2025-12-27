/**
 * HTTP client for role and permission operations.
 */

import axios, { AxiosInstance, AxiosError } from 'axios';
import {
  AssignRoleRequest,
  CreateRoleRequest,
  ListRolesParams,
  PermissionCheckRequest,
  PermissionCheckResult,
  Role,
  RoleListResponse,
  UpdateRoleRequest,
  UserRolesResponse,
} from './types';
import {
  RoleNotFoundError,
  RoleSlugExistsError,
  SystemRoleError,
  RoleInUseError,
} from './errors';

export interface RoleClientConfig {
  baseUrl: string;
  accessToken?: string;
  timeout?: number;
}

/**
 * Check if a permission matches a pattern (supports wildcards).
 */
export function matchesPermission(
  userPermission: string,
  requiredPermission: string
): boolean {
  // Full wildcard
  if (userPermission === '*' || userPermission === '*:*') {
    return true;
  }

  const [userResource, userAction] = userPermission.split(':');
  const [reqResource, reqAction] = requiredPermission.split(':');

  // Resource wildcard
  if (userResource === '*') {
    return userAction === '*' || userAction === reqAction;
  }

  // Same resource
  if (userResource === reqResource) {
    return userAction === '*' || userAction === reqAction;
  }

  return false;
}

/**
 * Check if any user permission matches the required permission.
 */
export function hasAnyPermission(
  userPermissions: string[],
  requiredPermission: string
): boolean {
  return userPermissions.some((p) => matchesPermission(p, requiredPermission));
}

/**
 * Check if user has all required permissions.
 */
export function hasAllPermissions(
  userPermissions: string[],
  requiredPermissions: string[]
): boolean {
  return requiredPermissions.every((required) =>
    userPermissions.some((p) => matchesPermission(p, required))
  );
}

export class RoleClient {
  private client: AxiosInstance;

  constructor(config: RoleClientConfig) {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (config.accessToken) {
      headers['Authorization'] = `Bearer ${config.accessToken}`;
    }

    this.client = axios.create({
      baseURL: config.baseUrl.replace(/\/$/, ''),
      timeout: config.timeout || 30000,
      headers,
    });
  }

  setAccessToken(token: string): void {
    this.client.defaults.headers['Authorization'] = `Bearer ${token}`;
  }

  private handleError(error: unknown, roleId?: string): never {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<{ error?: string; code?: string }>;
      const status = axiosError.response?.status;
      const errorCode = axiosError.response?.data?.code;

      if (status === 404) {
        throw new RoleNotFoundError(roleId || 'unknown');
      }
      if (status === 409 && errorCode === 'slug_exists') {
        throw new RoleSlugExistsError(roleId || 'unknown');
      }
      if (status === 403 && errorCode === 'system_role') {
        throw new SystemRoleError(roleId || 'unknown', 'modify');
      }
      if (status === 409 && errorCode === 'role_in_use') {
        throw new RoleInUseError(roleId || 'unknown', 0);
      }
    }
    throw error;
  }

  // Role CRUD Operations

  async list(params: ListRolesParams = {}): Promise<RoleListResponse> {
    const response = await this.client.get('/roles', {
      params: {
        page: params.page || 1,
        page_size: params.pageSize || 20,
        is_active: params.isActive,
        is_system: params.isSystem,
        search: params.search,
        sort: params.sort || 'hierarchy_level:asc',
      },
    });
    return response.data;
  }

  async get(roleId: string): Promise<Role> {
    try {
      const response = await this.client.get(`/roles/${roleId}`);
      return response.data;
    } catch (error) {
      this.handleError(error, roleId);
    }
  }

  async getBySlug(slug: string): Promise<Role> {
    try {
      const response = await this.client.get(`/roles/slug/${slug}`);
      return response.data;
    } catch (error) {
      this.handleError(error, slug);
    }
  }

  async create(request: CreateRoleRequest): Promise<Role> {
    try {
      const response = await this.client.post('/roles', request);
      return response.data;
    } catch (error) {
      this.handleError(error, request.slug);
    }
  }

  async update(roleId: string, request: UpdateRoleRequest): Promise<Role> {
    try {
      const response = await this.client.put(`/roles/${roleId}`, request);
      return response.data;
    } catch (error) {
      this.handleError(error, roleId);
    }
  }

  async delete(roleId: string): Promise<void> {
    try {
      await this.client.delete(`/roles/${roleId}`);
    } catch (error) {
      this.handleError(error, roleId);
    }
  }

  // User Role Assignment

  async getUserRoles(userId: string): Promise<UserRolesResponse> {
    const response = await this.client.get(`/users/${userId}/roles`);
    return response.data;
  }

  async assignRole(userId: string, request: AssignRoleRequest): Promise<void> {
    try {
      await this.client.post(`/users/${userId}/roles`, request);
    } catch (error) {
      this.handleError(error, request.roleId);
    }
  }

  async removeRole(userId: string, roleId: string): Promise<void> {
    try {
      await this.client.delete(`/users/${userId}/roles/${roleId}`);
    } catch (error) {
      this.handleError(error, roleId);
    }
  }

  // Permission Checking

  async checkPermission(
    request: PermissionCheckRequest
  ): Promise<PermissionCheckResult> {
    const response = await this.client.post('/permissions/check', request);
    return response.data;
  }
}
