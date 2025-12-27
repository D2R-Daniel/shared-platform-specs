/**
 * Audit logging client for tracking user actions and system events.
 */

import axios, { AxiosInstance } from 'axios';
import type {
  AuditEvent,
  AuditLogEntry,
  AuditLogListResponse,
  ListAuditLogsParams,
} from './types';

export interface AuditClientConfig {
  baseUrl: string;
  accessToken?: string;
  timeout?: number;
}

export class AuditClient {
  private client: AxiosInstance;
  private accessToken?: string;

  constructor(config: AuditClientConfig) {
    this.accessToken = config.accessToken;
    this.client = axios.create({
      baseURL: config.baseUrl,
      timeout: config.timeout || 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (this.accessToken) {
      this.client.defaults.headers.common['Authorization'] = `Bearer ${this.accessToken}`;
    }
  }

  /**
   * Set the access token for authenticated requests.
   */
  setAccessToken(token: string): void {
    this.accessToken = token;
    this.client.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  /**
   * Log an audit event.
   */
  async log(
    event: AuditEvent,
    options?: { ipAddress?: string; userAgent?: string }
  ): Promise<AuditLogEntry> {
    const data = {
      ...event,
      ipAddress: options?.ipAddress,
      userAgent: options?.userAgent,
    };

    const response = await this.client.post<AuditLogEntry>('/api/audit', data);
    return response.data;
  }

  /**
   * List audit log entries with optional filtering.
   */
  async list(params?: ListAuditLogsParams): Promise<AuditLogListResponse> {
    const queryParams: Record<string, string | number> = {};

    if (params?.page) queryParams.page = params.page;
    if (params?.pageSize) queryParams.page_size = params.pageSize;
    if (params?.eventType) queryParams.event_type = params.eventType;
    if (params?.actorId) queryParams.actor_id = params.actorId;
    if (params?.resourceType) queryParams.resource_type = params.resourceType;
    if (params?.resourceId) queryParams.resource_id = params.resourceId;
    if (params?.startDate) {
      queryParams.start_date =
        params.startDate instanceof Date
          ? params.startDate.toISOString()
          : params.startDate;
    }
    if (params?.endDate) {
      queryParams.end_date =
        params.endDate instanceof Date
          ? params.endDate.toISOString()
          : params.endDate;
    }

    const response = await this.client.get<AuditLogListResponse>('/api/audit', {
      params: queryParams,
    });
    return response.data;
  }

  /**
   * Get a specific audit log entry.
   */
  async get(entryId: string): Promise<AuditLogEntry> {
    const response = await this.client.get<AuditLogEntry>(`/api/audit/${entryId}`);
    return response.data;
  }

  /**
   * Get audit log entries for a specific resource.
   */
  async getByResource(
    resourceType: string,
    resourceId: string,
    params?: { page?: number; pageSize?: number }
  ): Promise<AuditLogListResponse> {
    return this.list({
      resourceType,
      resourceId,
      ...params,
    });
  }

  /**
   * Get audit log entries for a specific actor/user.
   */
  async getByActor(
    actorId: string,
    params?: { page?: number; pageSize?: number }
  ): Promise<AuditLogListResponse> {
    return this.list({
      actorId,
      ...params,
    });
  }
}
