/**
 * Audit logging type definitions.
 */

export type AuditEventType =
  // Authentication events
  | 'auth.login.success'
  | 'auth.login.failure'
  | 'auth.logout'
  | 'auth.password.change'
  | 'auth.password.reset'
  | 'auth.mfa.enabled'
  | 'auth.mfa.disabled'
  // User management events
  | 'user.created'
  | 'user.updated'
  | 'user.deleted'
  | 'user.suspended'
  | 'user.activated'
  | 'user.role.assigned'
  | 'user.role.removed'
  // Resource events
  | 'resource.created'
  | 'resource.updated'
  | 'resource.deleted'
  | 'resource.accessed'
  // Settings events
  | 'settings.updated'
  // System events
  | 'system.error'
  | 'system.warning'
  // Custom
  | 'custom';

export interface AuditEvent {
  eventType: AuditEventType;
  action: string;
  resourceType?: string;
  resourceId?: string;
  description?: string;
  metadata?: Record<string, any>;
  ipAddress?: string;
  userAgent?: string;
}

export interface AuditLogEntry {
  id: string;
  eventType: string;
  action: string;
  actorId?: string;
  actorEmail?: string;
  actorName?: string;
  resourceType?: string;
  resourceId?: string;
  description?: string;
  metadata?: Record<string, any>;
  ipAddress?: string;
  userAgent?: string;
  tenantId?: string;
  timestamp: string;
  createdAt: string;
}

export interface Pagination {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
}

export interface AuditLogListResponse {
  data: AuditLogEntry[];
  pagination: Pagination;
}

export interface CreateAuditEventRequest {
  eventType: AuditEventType;
  action: string;
  resourceType?: string;
  resourceId?: string;
  description?: string;
  metadata?: Record<string, any>;
}

export interface ListAuditLogsParams {
  page?: number;
  pageSize?: number;
  eventType?: string;
  actorId?: string;
  resourceType?: string;
  resourceId?: string;
  startDate?: Date | string;
  endDate?: Date | string;
}
