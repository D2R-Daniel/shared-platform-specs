/**
 * HTTP clients for tenant and department operations.
 */

import axios, { AxiosInstance, AxiosError } from 'axios';
import {
  CreateDepartmentRequest,
  CreateTenantRequest,
  Department,
  DepartmentListResponse,
  DepartmentMembersResponse,
  DepartmentTree,
  DepartmentWithDetails,
  GetDepartmentTreeParams,
  ListDepartmentMembersParams,
  ListDepartmentsParams,
  ListTenantsParams,
  SSOConfig,
  SSOSyncResult,
  SSOTestResult,
  Tenant,
  TenantListResponse,
  TenantStatus,
  UpdateDepartmentRequest,
  UpdateSSOConfigRequest,
  UpdateTenantRequest,
} from './types';
import {
  DepartmentNotFoundError,
  SSOConfigNotFoundError,
  TenantNotFoundError,
} from './errors';

export interface TenantClientConfig {
  baseUrl: string;
  accessToken?: string;
  timeout?: number;
}

export class TenantClient {
  private client: AxiosInstance;

  constructor(config: TenantClientConfig) {
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

  private handleError(error: unknown, entityId?: string): never {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError;
      if (axiosError.response?.status === 404) {
        throw new TenantNotFoundError(entityId || 'unknown');
      }
    }
    throw error;
  }

  // Tenant Operations

  async list(params: ListTenantsParams = {}): Promise<TenantListResponse> {
    const response = await this.client.get('/tenants', {
      params: {
        page: params.page || 1,
        page_size: params.pageSize || 20,
        status: params.status,
        plan: params.plan,
        search: params.search,
        sort: params.sort || 'created_at:desc',
      },
    });
    return response.data;
  }

  async get(tenantId: string): Promise<Tenant> {
    try {
      const response = await this.client.get(`/tenants/${tenantId}`);
      return response.data;
    } catch (error) {
      this.handleError(error, tenantId);
    }
  }

  async create(request: CreateTenantRequest): Promise<Tenant> {
    const response = await this.client.post('/tenants', request);
    return response.data;
  }

  async update(tenantId: string, request: UpdateTenantRequest): Promise<Tenant> {
    try {
      const response = await this.client.put(`/tenants/${tenantId}`, request);
      return response.data;
    } catch (error) {
      this.handleError(error, tenantId);
    }
  }

  async delete(tenantId: string): Promise<void> {
    try {
      await this.client.delete(`/tenants/${tenantId}`);
    } catch (error) {
      this.handleError(error, tenantId);
    }
  }

  async updateStatus(
    tenantId: string,
    status: TenantStatus,
    reason?: string
  ): Promise<Tenant> {
    try {
      const response = await this.client.patch(`/tenants/${tenantId}/status`, {
        status,
        reason,
      });
      return response.data;
    } catch (error) {
      this.handleError(error, tenantId);
    }
  }

  // SSO Operations

  async getSSOConfig(tenantId: string): Promise<SSOConfig> {
    try {
      const response = await this.client.get(`/tenants/${tenantId}/sso`);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        throw new SSOConfigNotFoundError(tenantId);
      }
      throw error;
    }
  }

  async updateSSOConfig(
    tenantId: string,
    request: UpdateSSOConfigRequest
  ): Promise<SSOConfig> {
    try {
      const response = await this.client.put(`/tenants/${tenantId}/sso`, request);
      return response.data;
    } catch (error) {
      this.handleError(error, tenantId);
    }
  }

  async deleteSSOConfig(tenantId: string): Promise<void> {
    try {
      await this.client.delete(`/tenants/${tenantId}/sso`);
    } catch (error) {
      this.handleError(error, tenantId);
    }
  }

  async testSSOConnection(tenantId: string): Promise<SSOTestResult> {
    try {
      const response = await this.client.post(`/tenants/${tenantId}/sso/test`);
      return response.data;
    } catch (error) {
      this.handleError(error, tenantId);
    }
  }

  async triggerSSOSync(tenantId: string): Promise<SSOSyncResult> {
    try {
      const response = await this.client.post(`/tenants/${tenantId}/sso/sync`);
      return response.data;
    } catch (error) {
      this.handleError(error, tenantId);
    }
  }
}

export interface DepartmentClientConfig {
  baseUrl: string;
  accessToken?: string;
  timeout?: number;
}

export class DepartmentClient {
  private client: AxiosInstance;

  constructor(config: DepartmentClientConfig) {
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

  private handleError(error: unknown, departmentId?: string): never {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError;
      if (axiosError.response?.status === 404) {
        throw new DepartmentNotFoundError(departmentId || 'unknown');
      }
    }
    throw error;
  }

  // Department Operations

  async list(params: ListDepartmentsParams = {}): Promise<DepartmentListResponse> {
    const response = await this.client.get('/departments', {
      params: {
        page: params.page || 1,
        page_size: params.pageSize || 20,
        parent_id: params.parentId,
        is_active: params.isActive,
        search: params.search,
        include_children: params.includeChildren || false,
        sort: params.sort || 'name:asc',
      },
    });
    return response.data;
  }

  async getTree(params: GetDepartmentTreeParams = {}): Promise<DepartmentTree[]> {
    const response = await this.client.get('/departments/tree', {
      params: {
        root_id: params.rootId,
        max_depth: params.maxDepth || 10,
        include_members: params.includeMembers || false,
      },
    });
    return response.data.data || response.data;
  }

  async get(
    departmentId: string,
    options: { includeHead?: boolean; includeParent?: boolean } = {}
  ): Promise<DepartmentWithDetails> {
    try {
      const response = await this.client.get(`/departments/${departmentId}`, {
        params: {
          include_head: options.includeHead || false,
          include_parent: options.includeParent || false,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(error, departmentId);
    }
  }

  async create(request: CreateDepartmentRequest): Promise<Department> {
    const response = await this.client.post('/departments', request);
    return response.data;
  }

  async update(
    departmentId: string,
    request: UpdateDepartmentRequest
  ): Promise<Department> {
    try {
      const response = await this.client.put(
        `/departments/${departmentId}`,
        request
      );
      return response.data;
    } catch (error) {
      this.handleError(error, departmentId);
    }
  }

  async delete(departmentId: string, force = false): Promise<void> {
    try {
      await this.client.delete(`/departments/${departmentId}`, {
        params: force ? { force: true } : {},
      });
    } catch (error) {
      this.handleError(error, departmentId);
    }
  }

  async getMembers(
    departmentId: string,
    params: ListDepartmentMembersParams = {}
  ): Promise<DepartmentMembersResponse> {
    try {
      const response = await this.client.get(
        `/departments/${departmentId}/members`,
        {
          params: {
            page: params.page || 1,
            page_size: params.pageSize || 20,
            include_subdepartments: params.includeSubdepartments || false,
            status: params.status || 'active',
          },
        }
      );
      return response.data;
    } catch (error) {
      this.handleError(error, departmentId);
    }
  }

  async move(departmentId: string, newParentId?: string): Promise<Department> {
    try {
      const response = await this.client.post(`/departments/${departmentId}/move`, {
        new_parent_id: newParentId,
      });
      return response.data;
    } catch (error) {
      this.handleError(error, departmentId);
    }
  }
}
