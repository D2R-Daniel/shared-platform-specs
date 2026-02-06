# Phase 0 Stream B: Audit Logs Module

**Feature**: Comprehensive audit logging with hash-chain integrity, SIEM streaming, and alert rules
**Goal**: Build a production-grade audit logging module that captures all platform events with cryptographic integrity verification, real-time streaming to SIEM tools, and configurable alert rules.
**Architecture**: Enhance existing `src/audit/` module with expanded API surface, hash-chain integrity, SIEM streaming, alert rules, and export capabilities.
**Tech Stack**: TypeScript, Node.js, Vitest, fetch (native) -- migrating from current Axios-based client to fetch-based pattern for consistency with newer modules (settings, webhooks)

---

## Current State Analysis

The existing `src/audit/` module provides a minimal Axios-based client with:
- `log(event)` -- log a single audit event
- `list(params?)` -- list with basic filtering (eventType, actorId, resourceType, resourceId, date range)
- `get(entryId)` -- fetch single entry
- `getByResource(type, id)` -- convenience wrapper around `list()`
- `getByActor(actorId)` -- convenience wrapper around `list()`

Types defined: `AuditEventType` (union of ~25 string literals), `AuditEvent`, `AuditLogEntry`, `AuditLogListResponse`, `CreateAuditEventRequest`, `ListAuditLogsParams`, `Pagination`.

No error classes exist. No barrel export of errors. The client uses Axios directly rather than a private `request()` helper.

### Migration Decisions

1. **HTTP client**: Migrate from Axios to native `fetch` with a private `request<T>()` helper, matching the pattern in `WebhookClient` and `SettingsClient`.
2. **Error handling**: Add `errors.ts` with a base `AuditError` class and specific subclasses, following the `WebhookError`/`SettingsError` pattern.
3. **Config interface**: Rename `AuditClientConfig` to `AuditClientOptions` for consistency with `WebhookClientOptions`/`SettingsClientOptions`.
4. **Backward compatibility**: Preserve existing method signatures; extend, do not break.

---

## Tasks

### Task 1: Define Core Model Types

**Files**: modify `packages/node/src/audit/types.ts`
**Steps**:

1. Replace the existing `types.ts` entirely with the expanded type definitions. Keep backward-compatible type aliases where needed.

2. Define the following types and interfaces:

```typescript
/**
 * Audit logging type definitions.
 */

// ---------------------------------------------------------------------------
// Enums as union types
// ---------------------------------------------------------------------------

export type AuditSeverity = 'info' | 'warning' | 'critical';

export type AuditActorType = 'user' | 'service' | 'system' | 'api_key';

export type AuditSource = 'api' | 'dashboard' | 'system' | 'webhook';

export type ExportFormat = 'json' | 'csv' | 'cef';

export type ExportDestinationType = 'download' | 's3' | 'gcs';

export type ExportStatus = 'processing' | 'completed' | 'failed';

export type ArchiveFormat = 'json' | 'parquet';

export type StreamDestinationType =
  | 'webhook'
  | 'datadog'
  | 'splunk'
  | 's3'
  | 'gcs'
  | 'http';

export type AlertNotificationChannelType = 'email' | 'webhook' | 'slack';

// ---------------------------------------------------------------------------
// Event type union (expanded, backward-compatible)
// ---------------------------------------------------------------------------

export type AuditEventType =
  // Authentication events
  | 'auth.login.success'
  | 'auth.login.failure'
  | 'auth.logout'
  | 'auth.password.change'
  | 'auth.password.reset'
  | 'auth.mfa.enabled'
  | 'auth.mfa.disabled'
  | 'auth.session.created'
  | 'auth.session.revoked'
  | 'auth.token.refreshed'
  // User management events
  | 'user.created'
  | 'user.updated'
  | 'user.deleted'
  | 'user.suspended'
  | 'user.activated'
  | 'user.role.assigned'
  | 'user.role.removed'
  | 'user.invited'
  | 'user.invitation.accepted'
  // Resource events
  | 'resource.created'
  | 'resource.updated'
  | 'resource.deleted'
  | 'resource.accessed'
  // Team events
  | 'team.created'
  | 'team.updated'
  | 'team.deleted'
  | 'team.member.added'
  | 'team.member.removed'
  | 'team.member.role.changed'
  // Settings events
  | 'settings.updated'
  | 'settings.reset'
  // Webhook events
  | 'webhook.created'
  | 'webhook.updated'
  | 'webhook.deleted'
  // API Key events
  | 'apikey.created'
  | 'apikey.revoked'
  | 'apikey.rotated'
  // Audit system events
  | 'audit.export.requested'
  | 'audit.export.completed'
  | 'audit.integrity.violation'
  | 'audit.stream.created'
  | 'audit.stream.failed'
  | 'audit.alert.triggered'
  // System events
  | 'system.error'
  | 'system.warning'
  // Custom
  | 'custom'
  | string; // Allow custom event type strings

// ---------------------------------------------------------------------------
// Core models
// ---------------------------------------------------------------------------

export interface GeoLocation {
  country: string;
  country_code: string;
  region?: string;
  city?: string;
  latitude?: number;
  longitude?: number;
  timezone?: string;
}

export interface AuditActor {
  id: string;
  type: AuditActorType;
  name?: string;
  email?: string;
  ip_address?: string;
  user_agent?: string;
  metadata?: Record<string, unknown>;
}

export interface AuditTarget {
  id: string;
  type: string;
  name?: string;
  metadata?: Record<string, unknown>;
}

export interface AuditChange {
  field: string;
  old_value?: unknown;
  new_value?: unknown;
  type?: string;
}

export interface AuditContext {
  ip_address?: string;
  user_agent?: string;
  geo_location?: GeoLocation;
  session_id?: string;
  request_id?: string;
  source?: AuditSource;
}

export interface AuditLogEntry {
  id: string;
  tenant_id: string;
  event_type: AuditEventType;
  action: string;
  description?: string;
  actor: AuditActor;
  targets: AuditTarget[];
  changes: AuditChange[];
  metadata?: Record<string, unknown>;
  severity: AuditSeverity;
  context?: AuditContext;
  integrity_hash?: string;
  previous_hash?: string;
  idempotency_key?: string;
  created_at: string;
}

export interface Pagination {
  page: number;
  page_size: number;
  total_items: number;
  total_pages: number;
  has_next?: boolean;
  has_previous?: boolean;
}

export interface AuditLogListResponse {
  data: AuditLogEntry[];
  pagination: Pagination;
}

// ---------------------------------------------------------------------------
// Request / query types
// ---------------------------------------------------------------------------

export interface CreateAuditEventRequest {
  event_type: AuditEventType;
  action: string;
  description?: string;
  actor: AuditActor;
  targets?: AuditTarget[];
  changes?: AuditChange[];
  metadata?: Record<string, unknown>;
  severity?: AuditSeverity;
  idempotency_key?: string;
}

export interface BatchAuditResult {
  logged_count: number;
  failed_count: number;
  errors: Array<{ index: number; message: string }>;
}

export interface AuditLogQuery {
  event_type?: AuditEventType;
  event_types?: AuditEventType[];
  actor_id?: string;
  actor_type?: AuditActorType;
  target_id?: string;
  target_type?: string;
  severity?: AuditSeverity;
  start_date?: Date | string;
  end_date?: Date | string;
  search?: string;
  sort?: string;
  page?: number;
  page_size?: number;
}
```

3. Add backward-compatible aliases at the bottom of the file:

```typescript
// ---------------------------------------------------------------------------
// Backward-compatible aliases (deprecated -- use new names)
// ---------------------------------------------------------------------------

/** @deprecated Use AuditLogQuery instead */
export type ListAuditLogsParams = AuditLogQuery;

/** @deprecated Use CreateAuditEventRequest instead */
export type AuditEvent = CreateAuditEventRequest;
```

**Tests**: No runtime tests needed for pure type definitions. Type checking via `npm run typecheck` validates correctness.
**Commit**: `feat(audit): expand core model types with actor, target, change, context, and severity`

---

### Task 2: Define Event Type, Retention, and Export Types

**Files**: modify `packages/node/src/audit/types.ts` (append to file from Task 1)
**Steps**:

1. Append the following interfaces after the core models:

```typescript
// ---------------------------------------------------------------------------
// Event type management
// ---------------------------------------------------------------------------

export interface AuditEventTypeDefinition {
  name: string;
  category: string;
  description?: string;
  severity: AuditSeverity;
  schema?: Record<string, unknown>; // JSON Schema
  auto_capture: boolean;
  version: number;
}

export interface CreateEventTypeRequest {
  name: string;
  category: string;
  description?: string;
  severity?: AuditSeverity;
  schema?: Record<string, unknown>;
  auto_capture?: boolean;
}

export interface UpdateEventTypeRequest {
  description?: string;
  severity?: AuditSeverity;
  schema?: Record<string, unknown>;
  auto_capture?: boolean;
}

// ---------------------------------------------------------------------------
// Retention policy
// ---------------------------------------------------------------------------

export interface RetentionPolicy {
  retention_days: number; // minimum 90
  archive_enabled: boolean;
  archive_destination?: string;
  archive_format?: ArchiveFormat;
  auto_delete_after_archive: boolean;
}

// ---------------------------------------------------------------------------
// Export
// ---------------------------------------------------------------------------

export interface ExportDestinationConfig {
  bucket?: string;
  prefix?: string;
  region?: string;
  credentials?: Record<string, string>;
}

export interface ExportRequest {
  query: AuditLogQuery;
  format: ExportFormat;
  destination?: ExportDestinationType;
  destination_config?: ExportDestinationConfig;
}

export interface ExportResult {
  export_id: string;
  status: ExportStatus;
  record_count?: number;
  file_size_bytes?: number;
  download_url?: string;
  expires_at?: string;
  started_at: string;
  completed_at?: string;
}
```

**Tests**: Type checking only.
**Commit**: `feat(audit): add event type, retention policy, and export types`

---

### Task 3: Define Integrity, Streaming, Alert, and Portal Types

**Files**: modify `packages/node/src/audit/types.ts` (append to file from Task 2)
**Steps**:

1. Append the following interfaces:

```typescript
// ---------------------------------------------------------------------------
// Integrity verification
// ---------------------------------------------------------------------------

export interface IntegrityVerificationResult {
  verified: boolean;
  entries_checked: number;
  first_invalid_entry_id?: string;
  reason?: string;
  verified_range: {
    start_date: string;
    end_date: string;
  };
}

export interface IntegrityProof {
  entry_id: string;
  integrity_hash: string;
  previous_hash: string;
  chain_position: number;
  verification_data: Record<string, unknown>;
}

// ---------------------------------------------------------------------------
// SIEM streaming
// ---------------------------------------------------------------------------

export interface StreamFilter {
  event_types?: AuditEventType[];
  severity?: AuditSeverity[];
  actor_types?: AuditActorType[];
}

export interface AuditStream {
  id: string;
  name: string;
  description?: string;
  destination_type: StreamDestinationType;
  destination_config: Record<string, unknown>;
  filter?: StreamFilter;
  is_active: boolean;
  last_delivery_at?: string;
  error_count: number;
  created_at: string;
  updated_at?: string;
}

export interface CreateStreamRequest {
  name: string;
  description?: string;
  destination_type: StreamDestinationType;
  destination_config: Record<string, unknown>;
  filter?: StreamFilter;
  is_active?: boolean;
}

export interface UpdateStreamRequest {
  name?: string;
  description?: string;
  destination_config?: Record<string, unknown>;
  filter?: StreamFilter;
  is_active?: boolean;
}

export interface StreamTestResult {
  success: boolean;
  message?: string;
  latency_ms?: number;
}

// ---------------------------------------------------------------------------
// Alert rules
// ---------------------------------------------------------------------------

export interface AlertCondition {
  event_type?: AuditEventType;
  event_types?: AuditEventType[];
  severity?: AuditSeverity;
  actor_type?: AuditActorType;
  count_threshold?: number;
  time_window_minutes?: number;
  group_by?: string;
}

export interface NotificationChannel {
  type: AlertNotificationChannelType;
  config: Record<string, unknown>;
}

export interface AlertRule {
  id: string;
  name: string;
  description?: string;
  condition: AlertCondition;
  notification_channels: NotificationChannel[];
  is_active: boolean;
  cooldown_minutes: number;
  last_triggered_at?: string;
  trigger_count: number;
  created_at: string;
  updated_at?: string;
}

export interface CreateAlertRuleRequest {
  name: string;
  description?: string;
  condition: AlertCondition;
  notification_channels: NotificationChannel[];
  is_active?: boolean;
  cooldown_minutes?: number;
}

export interface UpdateAlertRuleRequest {
  name?: string;
  description?: string;
  condition?: AlertCondition;
  notification_channels?: NotificationChannel[];
  is_active?: boolean;
  cooldown_minutes?: number;
}

export interface AlertRuleTestResult {
  would_trigger: boolean;
  matching_events_count: number;
  sample_events: AuditLogEntry[];
}

// ---------------------------------------------------------------------------
// Portal
// ---------------------------------------------------------------------------

export interface PortalLink {
  url: string;
  expires_at: string;
  organization_id: string;
}
```

**Tests**: Type checking only.
**Commit**: `feat(audit): add integrity, streaming, alert rule, and portal types`

---

### Task 4: Define Error Classes

**Files**: create `packages/node/src/audit/errors.ts`
**Steps**:

1. Create `errors.ts` following the `WebhookError` / `TeamError` pattern (base class with a `details` record):

```typescript
/**
 * Audit module errors.
 */

export class AuditError extends Error {
  public readonly details: Record<string, unknown>;

  constructor(message: string, details: Record<string, unknown> = {}) {
    super(message);
    this.name = 'AuditError';
    this.details = details;
  }
}

export class AuditEntryNotFoundError extends AuditError {
  constructor(entryId: string) {
    super(`Audit log entry not found: ${entryId}`, { entryId });
    this.name = 'AuditEntryNotFoundError';
  }
}

export class InvalidEventTypeError extends AuditError {
  constructor(eventType: string) {
    super(`Invalid audit event type: ${eventType}`, { eventType });
    this.name = 'InvalidEventTypeError';
  }
}

export class SchemaValidationError extends AuditError {
  constructor(eventType: string, message: string) {
    super(`Schema validation failed for event type '${eventType}': ${message}`, {
      eventType,
    });
    this.name = 'SchemaValidationError';
  }
}

export class ExportNotFoundError extends AuditError {
  constructor(exportId: string) {
    super(`Export not found: ${exportId}`, { exportId });
    this.name = 'ExportNotFoundError';
  }
}

export class ExportTooLargeError extends AuditError {
  constructor(message: string = 'Export exceeds maximum allowed size') {
    super(message);
    this.name = 'ExportTooLargeError';
  }
}

export class RetentionPolicyError extends AuditError {
  constructor(message: string) {
    super(`Retention policy error: ${message}`);
    this.name = 'RetentionPolicyError';
  }
}

export class IntegrityViolationError extends AuditError {
  constructor(entryId?: string) {
    super(
      entryId
        ? `Integrity violation detected at entry: ${entryId}`
        : 'Integrity violation detected',
      entryId ? { entryId } : {}
    );
    this.name = 'IntegrityViolationError';
  }
}

export class StreamNotFoundError extends AuditError {
  constructor(streamId: string) {
    super(`Audit stream not found: ${streamId}`, { streamId });
    this.name = 'StreamNotFoundError';
  }
}

export class StreamTestError extends AuditError {
  constructor(streamId: string, message: string) {
    super(`Stream test failed for '${streamId}': ${message}`, { streamId });
    this.name = 'StreamTestError';
  }
}

export class AlertRuleNotFoundError extends AuditError {
  constructor(ruleId: string) {
    super(`Alert rule not found: ${ruleId}`, { ruleId });
    this.name = 'AlertRuleNotFoundError';
  }
}

export class IdempotencyConflictError extends AuditError {
  constructor(idempotencyKey: string) {
    super(`Idempotency conflict: event already logged with key '${idempotencyKey}'`, {
      idempotencyKey,
    });
    this.name = 'IdempotencyConflictError';
  }
}
```

**Tests**: Unit tests in Task 14 will verify error instantiation.
**Commit**: `feat(audit): add comprehensive error classes`

---

### Task 5: Define Standard Event Type Constants

**Files**: create `packages/node/src/audit/events.ts`
**Steps**:

1. Create a constants file exporting all standard event type strings and a grouped lookup:

```typescript
/**
 * Standard audit event type constants.
 *
 * Use these constants instead of raw strings for type safety and discoverability.
 */

// Authentication events
export const AUTH_LOGIN_SUCCESS = 'auth.login.success' as const;
export const AUTH_LOGIN_FAILURE = 'auth.login.failure' as const;
export const AUTH_LOGOUT = 'auth.logout' as const;
export const AUTH_PASSWORD_CHANGE = 'auth.password.change' as const;
export const AUTH_PASSWORD_RESET = 'auth.password.reset' as const;
export const AUTH_MFA_ENABLED = 'auth.mfa.enabled' as const;
export const AUTH_MFA_DISABLED = 'auth.mfa.disabled' as const;
export const AUTH_SESSION_CREATED = 'auth.session.created' as const;
export const AUTH_SESSION_REVOKED = 'auth.session.revoked' as const;
export const AUTH_TOKEN_REFRESHED = 'auth.token.refreshed' as const;

// User management events
export const USER_CREATED = 'user.created' as const;
export const USER_UPDATED = 'user.updated' as const;
export const USER_DELETED = 'user.deleted' as const;
export const USER_SUSPENDED = 'user.suspended' as const;
export const USER_ACTIVATED = 'user.activated' as const;
export const USER_ROLE_ASSIGNED = 'user.role.assigned' as const;
export const USER_ROLE_REMOVED = 'user.role.removed' as const;
export const USER_INVITED = 'user.invited' as const;
export const USER_INVITATION_ACCEPTED = 'user.invitation.accepted' as const;

// Team events
export const TEAM_CREATED = 'team.created' as const;
export const TEAM_UPDATED = 'team.updated' as const;
export const TEAM_DELETED = 'team.deleted' as const;
export const TEAM_MEMBER_ADDED = 'team.member.added' as const;
export const TEAM_MEMBER_REMOVED = 'team.member.removed' as const;
export const TEAM_MEMBER_ROLE_CHANGED = 'team.member.role.changed' as const;

// Resource events
export const RESOURCE_CREATED = 'resource.created' as const;
export const RESOURCE_UPDATED = 'resource.updated' as const;
export const RESOURCE_DELETED = 'resource.deleted' as const;
export const RESOURCE_ACCESSED = 'resource.accessed' as const;

// Settings events
export const SETTINGS_UPDATED = 'settings.updated' as const;
export const SETTINGS_RESET = 'settings.reset' as const;

// Webhook events
export const WEBHOOK_CREATED = 'webhook.created' as const;
export const WEBHOOK_UPDATED = 'webhook.updated' as const;
export const WEBHOOK_DELETED = 'webhook.deleted' as const;

// API Key events
export const APIKEY_CREATED = 'apikey.created' as const;
export const APIKEY_REVOKED = 'apikey.revoked' as const;
export const APIKEY_ROTATED = 'apikey.rotated' as const;

// Audit system events
export const AUDIT_EXPORT_REQUESTED = 'audit.export.requested' as const;
export const AUDIT_EXPORT_COMPLETED = 'audit.export.completed' as const;
export const AUDIT_INTEGRITY_VIOLATION = 'audit.integrity.violation' as const;
export const AUDIT_STREAM_CREATED = 'audit.stream.created' as const;
export const AUDIT_STREAM_FAILED = 'audit.stream.failed' as const;
export const AUDIT_ALERT_TRIGGERED = 'audit.alert.triggered' as const;

// System events
export const SYSTEM_ERROR = 'system.error' as const;
export const SYSTEM_WARNING = 'system.warning' as const;

/**
 * All standard event types grouped by category.
 */
export const AUDIT_EVENT_CATEGORIES = {
  auth: [
    AUTH_LOGIN_SUCCESS,
    AUTH_LOGIN_FAILURE,
    AUTH_LOGOUT,
    AUTH_PASSWORD_CHANGE,
    AUTH_PASSWORD_RESET,
    AUTH_MFA_ENABLED,
    AUTH_MFA_DISABLED,
    AUTH_SESSION_CREATED,
    AUTH_SESSION_REVOKED,
    AUTH_TOKEN_REFRESHED,
  ],
  user: [
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_SUSPENDED,
    USER_ACTIVATED,
    USER_ROLE_ASSIGNED,
    USER_ROLE_REMOVED,
    USER_INVITED,
    USER_INVITATION_ACCEPTED,
  ],
  team: [
    TEAM_CREATED,
    TEAM_UPDATED,
    TEAM_DELETED,
    TEAM_MEMBER_ADDED,
    TEAM_MEMBER_REMOVED,
    TEAM_MEMBER_ROLE_CHANGED,
  ],
  resource: [
    RESOURCE_CREATED,
    RESOURCE_UPDATED,
    RESOURCE_DELETED,
    RESOURCE_ACCESSED,
  ],
  settings: [SETTINGS_UPDATED, SETTINGS_RESET],
  webhook: [WEBHOOK_CREATED, WEBHOOK_UPDATED, WEBHOOK_DELETED],
  apikey: [APIKEY_CREATED, APIKEY_REVOKED, APIKEY_ROTATED],
  audit: [
    AUDIT_EXPORT_REQUESTED,
    AUDIT_EXPORT_COMPLETED,
    AUDIT_INTEGRITY_VIOLATION,
    AUDIT_STREAM_CREATED,
    AUDIT_STREAM_FAILED,
    AUDIT_ALERT_TRIGGERED,
  ],
  system: [SYSTEM_ERROR, SYSTEM_WARNING],
} as const;

/**
 * Flat array of all standard event type strings.
 */
export const ALL_AUDIT_EVENT_TYPES = Object.values(AUDIT_EVENT_CATEGORIES).flat();
```

**Tests**: Verify `ALL_AUDIT_EVENT_TYPES` length and category membership in Task 14.
**Commit**: `feat(audit): add standard event type constants and category groupings`

---

### Task 6: Rewrite Client -- Base Infrastructure and Private `request()` Helper

**Files**: modify `packages/node/src/audit/client.ts`
**Steps**:

1. Replace the entire client file. Build the class skeleton with the private `request()` method, constructor, and `setAccessToken()`. This follows the `WebhookClient` fetch pattern exactly.

```typescript
/**
 * Audit logging client for tracking user actions and system events.
 *
 * Provides comprehensive audit logging with hash-chain integrity,
 * SIEM streaming, alert rules, and export capabilities.
 */

import type {
  AuditLogEntry,
  AuditLogListResponse,
  AuditLogQuery,
  CreateAuditEventRequest,
  BatchAuditResult,
  AuditEventTypeDefinition,
  CreateEventTypeRequest,
  UpdateEventTypeRequest,
  RetentionPolicy,
  ExportRequest,
  ExportResult,
  IntegrityVerificationResult,
  IntegrityProof,
  AuditStream,
  CreateStreamRequest,
  UpdateStreamRequest,
  StreamTestResult,
  AlertRule,
  CreateAlertRuleRequest,
  UpdateAlertRuleRequest,
  AlertRuleTestResult,
  PortalLink,
} from './types';
import {
  AuditError,
  AuditEntryNotFoundError,
  InvalidEventTypeError,
  SchemaValidationError,
  ExportNotFoundError,
  ExportTooLargeError,
  RetentionPolicyError,
  IntegrityViolationError,
  StreamNotFoundError,
  StreamTestError,
  AlertRuleNotFoundError,
  IdempotencyConflictError,
} from './errors';

export interface AuditClientOptions {
  baseUrl: string;
  accessToken?: string;
  timeout?: number;
}

/** @deprecated Use AuditClientOptions */
export type AuditClientConfig = AuditClientOptions;

export class AuditClient {
  private baseUrl: string;
  private accessToken?: string;
  private timeout: number;

  constructor(options: AuditClientOptions) {
    this.baseUrl = options.baseUrl.replace(/\/$/, '');
    this.accessToken = options.accessToken;
    this.timeout = options.timeout ?? 30000;
  }

  /**
   * Set the access token for authenticated requests.
   */
  setAccessToken(token: string): void {
    this.accessToken = token;
  }

  /**
   * Internal fetch wrapper with error mapping.
   */
  private async request<T>(
    method: string,
    path: string,
    body?: unknown,
    params?: Record<string, unknown>
  ): Promise<T> {
    const url = new URL(`${this.baseUrl}${path}`);
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          if (value instanceof Date) {
            url.searchParams.append(key, value.toISOString());
          } else if (Array.isArray(value)) {
            value.forEach((v) => url.searchParams.append(key, String(v)));
          } else {
            url.searchParams.append(key, String(value));
          }
        }
      });
    }

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.timeout);

    try {
      const response = await fetch(url.toString(), {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
        signal: controller.signal,
      });

      clearTimeout(timeoutId);

      if (!response.ok) {
        await this.handleErrorResponse(response);
      }

      if (response.status === 204) {
        return undefined as T;
      }

      return response.json();
    } finally {
      clearTimeout(timeoutId);
    }
  }

  /**
   * Map HTTP error responses to typed error classes.
   */
  private async handleErrorResponse(response: Response): Promise<never> {
    const status = response.status;
    let data: Record<string, unknown> = {};
    try {
      data = await response.json();
    } catch {
      // ignore parse failures
    }

    const errorCode = data.code as string | undefined;
    const errorMessage = (data.error as string) || (data.message as string) || response.statusText;
    const entityId = data.id as string | undefined;

    if (status === 400) {
      if (errorCode === 'invalid_event_type') {
        throw new InvalidEventTypeError(entityId || 'unknown');
      }
      if (errorCode === 'schema_validation_failed') {
        throw new SchemaValidationError(entityId || 'unknown', errorMessage);
      }
      if (errorCode === 'retention_policy_invalid') {
        throw new RetentionPolicyError(errorMessage);
      }
      if (errorCode === 'export_too_large') {
        throw new ExportTooLargeError(errorMessage);
      }
      throw new AuditError(`Bad request: ${errorMessage}`, { status, ...data });
    }

    if (status === 404) {
      if (errorCode === 'entry_not_found') {
        throw new AuditEntryNotFoundError(entityId || 'unknown');
      }
      if (errorCode === 'event_type_not_found') {
        throw new InvalidEventTypeError(entityId || 'unknown');
      }
      if (errorCode === 'export_not_found') {
        throw new ExportNotFoundError(entityId || 'unknown');
      }
      if (errorCode === 'stream_not_found') {
        throw new StreamNotFoundError(entityId || 'unknown');
      }
      if (errorCode === 'alert_rule_not_found') {
        throw new AlertRuleNotFoundError(entityId || 'unknown');
      }
      throw new AuditEntryNotFoundError(entityId || 'unknown');
    }

    if (status === 409) {
      if (errorCode === 'idempotency_conflict') {
        throw new IdempotencyConflictError(
          (data.idempotency_key as string) || 'unknown'
        );
      }
    }

    if (status === 422) {
      if (errorCode === 'integrity_violation') {
        throw new IntegrityViolationError(entityId);
      }
    }

    throw new AuditError(`HTTP ${status}: ${errorMessage}`, { status, ...data });
  }

  // ... methods added in subsequent tasks
}
```

**Tests**: Tests for the request helper are covered implicitly by all method tests. Specific error-mapping tests in Task 14.
**Commit**: `feat(audit): rewrite client base with fetch and error mapping`

---

### Task 7: Core CRUD Methods -- `log`, `logBatch`, `list`, `get`

**Files**: modify `packages/node/src/audit/client.ts` (add methods to `AuditClient` class)
**Steps**:

1. Add the four core methods inside the class:

```typescript
  // ---------------------------------------------------------------------------
  // Core audit log operations
  // ---------------------------------------------------------------------------

  /**
   * Log a single audit event.
   */
  async log(event: CreateAuditEventRequest): Promise<AuditLogEntry> {
    return this.request('POST', '/api/audit', event);
  }

  /**
   * Log a batch of audit events.
   *
   * Returns a summary of how many were logged vs. failed.
   */
  async logBatch(events: CreateAuditEventRequest[]): Promise<BatchAuditResult> {
    return this.request('POST', '/api/audit/batch', { events });
  }

  /**
   * List audit log entries with optional filtering and pagination.
   */
  async list(query?: AuditLogQuery): Promise<AuditLogListResponse> {
    const params: Record<string, unknown> = {};
    if (query) {
      if (query.event_type) params.event_type = query.event_type;
      if (query.event_types) params.event_types = query.event_types;
      if (query.actor_id) params.actor_id = query.actor_id;
      if (query.actor_type) params.actor_type = query.actor_type;
      if (query.target_id) params.target_id = query.target_id;
      if (query.target_type) params.target_type = query.target_type;
      if (query.severity) params.severity = query.severity;
      if (query.start_date) params.start_date = query.start_date;
      if (query.end_date) params.end_date = query.end_date;
      if (query.search) params.search = query.search;
      if (query.sort) params.sort = query.sort;
      if (query.page) params.page = query.page;
      if (query.page_size) params.page_size = query.page_size;
    }

    return this.request('GET', '/api/audit', undefined, params);
  }

  /**
   * Get a specific audit log entry by ID.
   */
  async get(entryId: string): Promise<AuditLogEntry> {
    try {
      return await this.request('GET', `/api/audit/${entryId}`);
    } catch (error) {
      if (error instanceof AuditEntryNotFoundError) {
        throw new AuditEntryNotFoundError(entryId);
      }
      throw error;
    }
  }
```

**Tests**:
- `log()` sends POST to `/api/audit` with event payload, returns entry
- `logBatch()` sends POST to `/api/audit/batch` with array, returns `BatchAuditResult`
- `list()` sends GET with query params correctly serialized
- `list()` with date range converts `Date` objects to ISO strings
- `get()` returns single entry; throws `AuditEntryNotFoundError` on 404

**Commit**: `feat(audit): add core CRUD methods -- log, logBatch, list, get`

---

### Task 8: Actor and Resource Query Methods

**Files**: modify `packages/node/src/audit/client.ts` (add methods)
**Steps**:

1. Add convenience query methods:

```typescript
  /**
   * Get audit log entries for a specific actor/user.
   */
  async getByActor(
    actorId: string,
    query?: Omit<AuditLogQuery, 'actor_id'>
  ): Promise<AuditLogListResponse> {
    return this.list({ ...query, actor_id: actorId });
  }

  /**
   * Get audit log entries for a specific resource.
   */
  async getByResource(
    targetType: string,
    targetId: string,
    query?: Omit<AuditLogQuery, 'target_type' | 'target_id'>
  ): Promise<AuditLogListResponse> {
    return this.list({ ...query, target_type: targetType, target_id: targetId });
  }
```

**Tests**:
- `getByActor('user-1')` delegates to `list()` with `actor_id: 'user-1'`
- `getByResource('team', 'team-1')` delegates to `list()` with `target_type` and `target_id`
- Both accept additional query params and merge them

**Commit**: `feat(audit): add getByActor and getByResource convenience methods`

---

### Task 9: Event Type Management Methods

**Files**: modify `packages/node/src/audit/client.ts` (add methods)
**Steps**:

1. Add event type CRUD:

```typescript
  // ---------------------------------------------------------------------------
  // Event type management
  // ---------------------------------------------------------------------------

  /**
   * List all registered event types.
   */
  async listEventTypes(): Promise<AuditEventTypeDefinition[]> {
    const response = await this.request<{ data: AuditEventTypeDefinition[] }>(
      'GET',
      '/api/audit/event-types'
    );
    return response.data;
  }

  /**
   * Get a specific event type definition.
   */
  async getEventType(name: string): Promise<AuditEventTypeDefinition> {
    try {
      return await this.request('GET', `/api/audit/event-types/${name}`);
    } catch (error) {
      if (error instanceof InvalidEventTypeError) {
        throw new InvalidEventTypeError(name);
      }
      throw error;
    }
  }

  /**
   * Create a custom event type definition.
   */
  async createEventType(
    definition: CreateEventTypeRequest
  ): Promise<AuditEventTypeDefinition> {
    return this.request('POST', '/api/audit/event-types', definition);
  }

  /**
   * Update an existing event type definition.
   */
  async updateEventType(
    name: string,
    definition: UpdateEventTypeRequest
  ): Promise<AuditEventTypeDefinition> {
    try {
      return await this.request('PUT', `/api/audit/event-types/${name}`, definition);
    } catch (error) {
      if (error instanceof InvalidEventTypeError) {
        throw new InvalidEventTypeError(name);
      }
      throw error;
    }
  }
```

**Tests**:
- `listEventTypes()` GET `/api/audit/event-types`, unwraps `data` array
- `getEventType('user.created')` GET `/api/audit/event-types/user.created`
- `getEventType('nonexistent')` throws `InvalidEventTypeError`
- `createEventType(...)` POST with definition body
- `updateEventType('user.created', ...)` PUT to named path

**Commit**: `feat(audit): add event type management methods`

---

### Task 10: Retention Policy and Export Methods

**Files**: modify `packages/node/src/audit/client.ts` (add methods)
**Steps**:

1. Add retention and export methods:

```typescript
  // ---------------------------------------------------------------------------
  // Retention policy
  // ---------------------------------------------------------------------------

  /**
   * Get the current retention policy for the tenant.
   */
  async getRetentionPolicy(): Promise<RetentionPolicy> {
    return this.request('GET', '/api/audit/retention');
  }

  /**
   * Set the retention policy for the tenant.
   *
   * @param policy - Retention policy to set. `retention_days` must be >= 90.
   */
  async setRetentionPolicy(policy: RetentionPolicy): Promise<RetentionPolicy> {
    return this.request('PUT', '/api/audit/retention', policy);
  }

  // ---------------------------------------------------------------------------
  // Export
  // ---------------------------------------------------------------------------

  /**
   * Start an asynchronous export of audit logs.
   */
  async exportLogs(request: ExportRequest): Promise<ExportResult> {
    return this.request('POST', '/api/audit/exports', request);
  }

  /**
   * Get the status of an export job.
   */
  async getExportStatus(exportId: string): Promise<ExportResult> {
    try {
      return await this.request('GET', `/api/audit/exports/${exportId}`);
    } catch (error) {
      if (error instanceof AuditEntryNotFoundError || error instanceof ExportNotFoundError) {
        throw new ExportNotFoundError(exportId);
      }
      throw error;
    }
  }

  /**
   * Download a completed export.
   *
   * Returns the export result with a populated `download_url`.
   */
  async downloadExport(exportId: string): Promise<ExportResult> {
    try {
      return await this.request('GET', `/api/audit/exports/${exportId}/download`);
    } catch (error) {
      if (error instanceof AuditEntryNotFoundError || error instanceof ExportNotFoundError) {
        throw new ExportNotFoundError(exportId);
      }
      throw error;
    }
  }
```

**Tests**:
- `getRetentionPolicy()` GET `/api/audit/retention`
- `setRetentionPolicy(...)` PUT `/api/audit/retention` with body; throws `RetentionPolicyError` for < 90 days on server
- `exportLogs(...)` POST `/api/audit/exports`
- `getExportStatus('exp-1')` GET to correct path; throws `ExportNotFoundError` on 404
- `downloadExport('exp-1')` GET with `/download` suffix

**Commit**: `feat(audit): add retention policy and export methods`

---

### Task 11: Integrity Verification Methods

**Files**: modify `packages/node/src/audit/client.ts` (add methods)
**Steps**:

1. Add integrity methods:

```typescript
  // ---------------------------------------------------------------------------
  // Integrity verification
  // ---------------------------------------------------------------------------

  /**
   * Verify the hash-chain integrity of audit logs in a date range.
   */
  async verifyIntegrity(
    startDate: Date | string,
    endDate: Date | string
  ): Promise<IntegrityVerificationResult> {
    return this.request('POST', '/api/audit/integrity/verify', {
      start_date: startDate instanceof Date ? startDate.toISOString() : startDate,
      end_date: endDate instanceof Date ? endDate.toISOString() : endDate,
    });
  }

  /**
   * Get the integrity proof for a specific audit log entry.
   */
  async getIntegrityProof(entryId: string): Promise<IntegrityProof> {
    try {
      return await this.request('GET', `/api/audit/integrity/${entryId}`);
    } catch (error) {
      if (error instanceof AuditEntryNotFoundError) {
        throw new AuditEntryNotFoundError(entryId);
      }
      throw error;
    }
  }
```

**Tests**:
- `verifyIntegrity(new Date('2024-01-01'), new Date('2024-02-01'))` POST with ISO date strings
- `verifyIntegrity('2024-01-01', '2024-02-01')` also works with string dates
- `getIntegrityProof('entry-1')` GET to correct path; returns proof object
- `getIntegrityProof('nonexistent')` throws `AuditEntryNotFoundError`

**Commit**: `feat(audit): add hash-chain integrity verification methods`

---

### Task 12: SIEM Streaming Methods

**Files**: modify `packages/node/src/audit/client.ts` (add methods)
**Steps**:

1. Add stream CRUD and test methods:

```typescript
  // ---------------------------------------------------------------------------
  // SIEM streaming
  // ---------------------------------------------------------------------------

  /**
   * Create a new audit log stream.
   */
  async createStream(config: CreateStreamRequest): Promise<AuditStream> {
    return this.request('POST', '/api/audit/streams', config);
  }

  /**
   * List all configured audit log streams.
   */
  async listStreams(): Promise<AuditStream[]> {
    const response = await this.request<{ data: AuditStream[] }>(
      'GET',
      '/api/audit/streams'
    );
    return response.data;
  }

  /**
   * Get a specific audit log stream by ID.
   */
  async getStream(streamId: string): Promise<AuditStream> {
    try {
      return await this.request('GET', `/api/audit/streams/${streamId}`);
    } catch (error) {
      if (error instanceof StreamNotFoundError) {
        throw new StreamNotFoundError(streamId);
      }
      throw error;
    }
  }

  /**
   * Update an existing audit log stream.
   */
  async updateStream(
    streamId: string,
    config: UpdateStreamRequest
  ): Promise<AuditStream> {
    try {
      return await this.request('PUT', `/api/audit/streams/${streamId}`, config);
    } catch (error) {
      if (error instanceof StreamNotFoundError) {
        throw new StreamNotFoundError(streamId);
      }
      throw error;
    }
  }

  /**
   * Delete an audit log stream.
   */
  async deleteStream(streamId: string): Promise<void> {
    try {
      await this.request('DELETE', `/api/audit/streams/${streamId}`);
    } catch (error) {
      if (error instanceof StreamNotFoundError) {
        throw new StreamNotFoundError(streamId);
      }
      throw error;
    }
  }

  /**
   * Test a stream's connectivity and configuration.
   */
  async testStream(streamId: string): Promise<StreamTestResult> {
    try {
      return await this.request('POST', `/api/audit/streams/${streamId}/test`);
    } catch (error) {
      if (error instanceof StreamNotFoundError) {
        throw new StreamNotFoundError(streamId);
      }
      throw error;
    }
  }
```

**Tests**:
- `createStream(...)` POST `/api/audit/streams` with config
- `listStreams()` GET and unwrap `data` array
- `getStream('stream-1')` GET; throws `StreamNotFoundError` on 404
- `updateStream('stream-1', ...)` PUT with partial config
- `deleteStream('stream-1')` DELETE; no return value
- `testStream('stream-1')` POST `/test` suffix; returns `StreamTestResult`

**Commit**: `feat(audit): add SIEM streaming CRUD and test methods`

---

### Task 13: Alert Rule and Portal Methods

**Files**: modify `packages/node/src/audit/client.ts` (add methods)
**Steps**:

1. Add alert rule CRUD, test, and portal methods:

```typescript
  // ---------------------------------------------------------------------------
  // Alert rules
  // ---------------------------------------------------------------------------

  /**
   * Create a new alert rule.
   */
  async createAlertRule(rule: CreateAlertRuleRequest): Promise<AlertRule> {
    return this.request('POST', '/api/audit/alerts', rule);
  }

  /**
   * List all alert rules.
   */
  async listAlertRules(): Promise<AlertRule[]> {
    const response = await this.request<{ data: AlertRule[] }>(
      'GET',
      '/api/audit/alerts'
    );
    return response.data;
  }

  /**
   * Get a specific alert rule by ID.
   */
  async getAlertRule(ruleId: string): Promise<AlertRule> {
    try {
      return await this.request('GET', `/api/audit/alerts/${ruleId}`);
    } catch (error) {
      if (error instanceof AlertRuleNotFoundError) {
        throw new AlertRuleNotFoundError(ruleId);
      }
      throw error;
    }
  }

  /**
   * Update an existing alert rule.
   */
  async updateAlertRule(
    ruleId: string,
    rule: UpdateAlertRuleRequest
  ): Promise<AlertRule> {
    try {
      return await this.request('PUT', `/api/audit/alerts/${ruleId}`, rule);
    } catch (error) {
      if (error instanceof AlertRuleNotFoundError) {
        throw new AlertRuleNotFoundError(ruleId);
      }
      throw error;
    }
  }

  /**
   * Delete an alert rule.
   */
  async deleteAlertRule(ruleId: string): Promise<void> {
    try {
      await this.request('DELETE', `/api/audit/alerts/${ruleId}`);
    } catch (error) {
      if (error instanceof AlertRuleNotFoundError) {
        throw new AlertRuleNotFoundError(ruleId);
      }
      throw error;
    }
  }

  /**
   * Test an alert rule against recent events without triggering it.
   */
  async testAlertRule(ruleId: string): Promise<AlertRuleTestResult> {
    try {
      return await this.request('POST', `/api/audit/alerts/${ruleId}/test`);
    } catch (error) {
      if (error instanceof AlertRuleNotFoundError) {
        throw new AlertRuleNotFoundError(ruleId);
      }
      throw error;
    }
  }

  // ---------------------------------------------------------------------------
  // Portal
  // ---------------------------------------------------------------------------

  /**
   * Generate a temporary portal link for viewing audit logs.
   *
   * @param orgId - Organization/tenant ID
   * @param ttl - Time-to-live in seconds (default: server-side default, typically 3600)
   */
  async generatePortalLink(orgId: string, ttl?: number): Promise<PortalLink> {
    const body: Record<string, unknown> = { organization_id: orgId };
    if (ttl !== undefined) {
      body.ttl = ttl;
    }
    return this.request('POST', '/api/audit/portal', body);
  }
```

**Tests**:
- Full CRUD cycle for alert rules: create, list, get, update, delete
- `testAlertRule('rule-1')` POST; returns `AlertRuleTestResult`
- 404 handling throws `AlertRuleNotFoundError` with correct ruleId
- `generatePortalLink('org-1', 7200)` POST with body `{ organization_id, ttl }`
- `generatePortalLink('org-1')` POST without `ttl`

**Commit**: `feat(audit): add alert rule CRUD, test, and portal link methods`

---

### Task 14: Test Suite -- Core Methods and Error Handling

**Files**: create `packages/node/src/__tests__/audit.test.ts`
**Steps**:

1. Create the test file using the established Vitest pattern. Since we are migrating to `fetch`, mock `global.fetch` instead of `axios`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AuditClient } from '../audit';
import {
  AuditEntryNotFoundError,
  InvalidEventTypeError,
  ExportNotFoundError,
  StreamNotFoundError,
  AlertRuleNotFoundError,
  IdempotencyConflictError,
} from '../audit/errors';
import type {
  AuditLogEntry,
  CreateAuditEventRequest,
  AuditLogListResponse,
} from '../audit/types';

// Helper to create a mock Response
function mockResponse(data: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    statusText: status === 200 ? 'OK' : 'Error',
    json: () => Promise.resolve(data),
    headers: new Headers(),
  } as Response;
}

function mock404(code: string, id?: string): Response {
  return mockResponse({ code, id, error: 'Not found' }, 404);
}

describe('AuditClient', () => {
  let client: AuditClient;
  let fetchMock: ReturnType<typeof vi.fn>;

  const sampleEntry: AuditLogEntry = {
    id: 'entry-1',
    tenant_id: 'tenant-1',
    event_type: 'user.created',
    action: 'create',
    description: 'User created',
    actor: { id: 'user-1', type: 'user', name: 'Admin' },
    targets: [{ id: 'user-2', type: 'user', name: 'New User' }],
    changes: [{ field: 'status', old_value: null, new_value: 'active' }],
    severity: 'info',
    created_at: '2024-01-01T00:00:00Z',
  };

  beforeEach(() => {
    fetchMock = vi.fn();
    global.fetch = fetchMock;
    client = new AuditClient({
      baseUrl: 'https://api.example.com',
      accessToken: 'test-token',
    });
  });

  describe('log', () => {
    it('should log a single audit event', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse(sampleEntry));

      const event: CreateAuditEventRequest = {
        event_type: 'user.created',
        action: 'create',
        actor: { id: 'user-1', type: 'user' },
      };

      const result = await client.log(event);

      expect(result.id).toBe('entry-1');
      expect(fetchMock).toHaveBeenCalledWith(
        'https://api.example.com/api/audit',
        expect.objectContaining({ method: 'POST' })
      );
    });
  });

  describe('logBatch', () => {
    it('should log a batch of audit events', async () => {
      const batchResult = { logged_count: 2, failed_count: 0, errors: [] };
      fetchMock.mockResolvedValueOnce(mockResponse(batchResult));

      const events: CreateAuditEventRequest[] = [
        { event_type: 'user.created', action: 'create', actor: { id: 'u1', type: 'user' } },
        { event_type: 'user.updated', action: 'update', actor: { id: 'u1', type: 'user' } },
      ];

      const result = await client.logBatch(events);

      expect(result.logged_count).toBe(2);
      expect(result.failed_count).toBe(0);
    });
  });

  describe('list', () => {
    it('should list audit entries with query params', async () => {
      const listResponse: AuditLogListResponse = {
        data: [sampleEntry],
        pagination: { page: 1, page_size: 20, total_items: 1, total_pages: 1 },
      };
      fetchMock.mockResolvedValueOnce(mockResponse(listResponse));

      const result = await client.list({
        event_type: 'user.created',
        severity: 'info',
        page: 1,
        page_size: 20,
      });

      expect(result.data).toHaveLength(1);
      const calledUrl = fetchMock.mock.calls[0][0] as string;
      expect(calledUrl).toContain('event_type=user.created');
      expect(calledUrl).toContain('severity=info');
    });

    it('should handle Date objects in query', async () => {
      fetchMock.mockResolvedValueOnce(
        mockResponse({ data: [], pagination: { page: 1, page_size: 20, total_items: 0, total_pages: 0 } })
      );

      await client.list({
        start_date: new Date('2024-01-01T00:00:00Z'),
        end_date: new Date('2024-02-01T00:00:00Z'),
      });

      const calledUrl = fetchMock.mock.calls[0][0] as string;
      expect(calledUrl).toContain('start_date=2024-01-01');
      expect(calledUrl).toContain('end_date=2024-02-01');
    });
  });

  describe('get', () => {
    it('should get a single audit entry', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse(sampleEntry));

      const result = await client.get('entry-1');

      expect(result.id).toBe('entry-1');
    });

    it('should throw AuditEntryNotFoundError on 404', async () => {
      fetchMock.mockResolvedValueOnce(mock404('entry_not_found', 'entry-999'));

      await expect(client.get('entry-999')).rejects.toThrow(AuditEntryNotFoundError);
    });
  });

  describe('getByActor', () => {
    it('should delegate to list with actor_id', async () => {
      fetchMock.mockResolvedValueOnce(
        mockResponse({ data: [sampleEntry], pagination: { page: 1, page_size: 20, total_items: 1, total_pages: 1 } })
      );

      await client.getByActor('user-1');

      const calledUrl = fetchMock.mock.calls[0][0] as string;
      expect(calledUrl).toContain('actor_id=user-1');
    });
  });

  describe('getByResource', () => {
    it('should delegate to list with target_type and target_id', async () => {
      fetchMock.mockResolvedValueOnce(
        mockResponse({ data: [], pagination: { page: 1, page_size: 20, total_items: 0, total_pages: 0 } })
      );

      await client.getByResource('team', 'team-1');

      const calledUrl = fetchMock.mock.calls[0][0] as string;
      expect(calledUrl).toContain('target_type=team');
      expect(calledUrl).toContain('target_id=team-1');
    });
  });

  // Additional sections tested in Task 15 and Task 16
});
```

**Tests**: This file IS the test. Run with `npm test -- --run src/__tests__/audit.test.ts`.
**Commit**: `test(audit): add unit tests for core CRUD methods and error handling`

---

### Task 15: Test Suite -- Event Types, Retention, Export, and Integrity

**Files**: modify `packages/node/src/__tests__/audit.test.ts` (append `describe` blocks)
**Steps**:

1. Add the following test blocks after the core tests in the same file:

```typescript
  describe('event types', () => {
    it('should list event types', async () => {
      const types = [{ name: 'user.created', category: 'user', severity: 'info', auto_capture: true, version: 1 }];
      fetchMock.mockResolvedValueOnce(mockResponse({ data: types }));

      const result = await client.listEventTypes();

      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('user.created');
    });

    it('should get event type by name', async () => {
      const eventType = { name: 'user.created', category: 'user', severity: 'info', auto_capture: true, version: 1 };
      fetchMock.mockResolvedValueOnce(mockResponse(eventType));

      const result = await client.getEventType('user.created');

      expect(result.name).toBe('user.created');
    });

    it('should throw InvalidEventTypeError for unknown event type', async () => {
      fetchMock.mockResolvedValueOnce(mock404('event_type_not_found', 'unknown.event'));

      await expect(client.getEventType('unknown.event')).rejects.toThrow(InvalidEventTypeError);
    });

    it('should create event type', async () => {
      const created = { name: 'custom.action', category: 'custom', severity: 'info', auto_capture: false, version: 1 };
      fetchMock.mockResolvedValueOnce(mockResponse(created));

      const result = await client.createEventType({
        name: 'custom.action',
        category: 'custom',
      });

      expect(result.name).toBe('custom.action');
    });

    it('should update event type', async () => {
      const updated = { name: 'user.created', category: 'user', description: 'Updated', severity: 'warning', auto_capture: true, version: 2 };
      fetchMock.mockResolvedValueOnce(mockResponse(updated));

      const result = await client.updateEventType('user.created', { severity: 'warning' });

      expect(result.severity).toBe('warning');
    });
  });

  describe('retention policy', () => {
    it('should get retention policy', async () => {
      const policy = { retention_days: 365, archive_enabled: true, archive_format: 'json', auto_delete_after_archive: false };
      fetchMock.mockResolvedValueOnce(mockResponse(policy));

      const result = await client.getRetentionPolicy();

      expect(result.retention_days).toBe(365);
    });

    it('should set retention policy', async () => {
      const policy = { retention_days: 180, archive_enabled: false, auto_delete_after_archive: false };
      fetchMock.mockResolvedValueOnce(mockResponse(policy));

      const result = await client.setRetentionPolicy(policy as any);

      expect(result.retention_days).toBe(180);
      expect(fetchMock).toHaveBeenCalledWith(
        'https://api.example.com/api/audit/retention',
        expect.objectContaining({ method: 'PUT' })
      );
    });
  });

  describe('exports', () => {
    it('should start an export', async () => {
      const exportResult = { export_id: 'exp-1', status: 'processing', started_at: '2024-01-01T00:00:00Z' };
      fetchMock.mockResolvedValueOnce(mockResponse(exportResult));

      const result = await client.exportLogs({
        query: { event_type: 'user.created' },
        format: 'json',
      });

      expect(result.export_id).toBe('exp-1');
      expect(result.status).toBe('processing');
    });

    it('should get export status', async () => {
      const exportResult = { export_id: 'exp-1', status: 'completed', record_count: 100, started_at: '2024-01-01T00:00:00Z' };
      fetchMock.mockResolvedValueOnce(mockResponse(exportResult));

      const result = await client.getExportStatus('exp-1');

      expect(result.status).toBe('completed');
    });

    it('should throw ExportNotFoundError on 404', async () => {
      fetchMock.mockResolvedValueOnce(mock404('export_not_found', 'exp-999'));

      await expect(client.getExportStatus('exp-999')).rejects.toThrow(ExportNotFoundError);
    });

    it('should download export', async () => {
      const exportResult = { export_id: 'exp-1', status: 'completed', download_url: 'https://cdn.example.com/export.json', started_at: '2024-01-01T00:00:00Z' };
      fetchMock.mockResolvedValueOnce(mockResponse(exportResult));

      const result = await client.downloadExport('exp-1');

      expect(result.download_url).toBeDefined();
    });
  });

  describe('integrity', () => {
    it('should verify integrity with Date objects', async () => {
      const verification = { verified: true, entries_checked: 500, verified_range: { start_date: '2024-01-01', end_date: '2024-02-01' } };
      fetchMock.mockResolvedValueOnce(mockResponse(verification));

      const result = await client.verifyIntegrity(
        new Date('2024-01-01T00:00:00Z'),
        new Date('2024-02-01T00:00:00Z')
      );

      expect(result.verified).toBe(true);
      expect(result.entries_checked).toBe(500);
    });

    it('should verify integrity with string dates', async () => {
      const verification = { verified: false, entries_checked: 100, first_invalid_entry_id: 'entry-50', reason: 'Hash mismatch', verified_range: { start_date: '2024-01-01', end_date: '2024-01-15' } };
      fetchMock.mockResolvedValueOnce(mockResponse(verification));

      const result = await client.verifyIntegrity('2024-01-01', '2024-01-15');

      expect(result.verified).toBe(false);
      expect(result.first_invalid_entry_id).toBe('entry-50');
    });

    it('should get integrity proof', async () => {
      const proof = { entry_id: 'entry-1', integrity_hash: 'abc123', previous_hash: 'xyz789', chain_position: 42, verification_data: {} };
      fetchMock.mockResolvedValueOnce(mockResponse(proof));

      const result = await client.getIntegrityProof('entry-1');

      expect(result.integrity_hash).toBe('abc123');
      expect(result.chain_position).toBe(42);
    });
  });
```

**Tests**: This IS the test code. Run with `npm test`.
**Commit**: `test(audit): add tests for event types, retention, exports, and integrity`

---

### Task 16: Test Suite -- Streams, Alerts, Portal, and Event Constants

**Files**: modify `packages/node/src/__tests__/audit.test.ts` (append `describe` blocks)
**Steps**:

1. Add stream and alert test blocks:

```typescript
  describe('streams', () => {
    const sampleStream = {
      id: 'stream-1',
      name: 'Splunk Stream',
      destination_type: 'splunk',
      destination_config: { url: 'https://splunk.example.com', token: 'xxx' },
      is_active: true,
      error_count: 0,
      created_at: '2024-01-01T00:00:00Z',
    };

    it('should create a stream', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse(sampleStream));

      const result = await client.createStream({
        name: 'Splunk Stream',
        destination_type: 'splunk',
        destination_config: { url: 'https://splunk.example.com', token: 'xxx' },
      });

      expect(result.id).toBe('stream-1');
    });

    it('should list streams', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse({ data: [sampleStream] }));

      const result = await client.listStreams();

      expect(result).toHaveLength(1);
    });

    it('should get stream by id', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse(sampleStream));

      const result = await client.getStream('stream-1');

      expect(result.name).toBe('Splunk Stream');
    });

    it('should throw StreamNotFoundError on 404', async () => {
      fetchMock.mockResolvedValueOnce(mock404('stream_not_found', 'stream-999'));

      await expect(client.getStream('stream-999')).rejects.toThrow(StreamNotFoundError);
    });

    it('should update stream', async () => {
      const updated = { ...sampleStream, is_active: false };
      fetchMock.mockResolvedValueOnce(mockResponse(updated));

      const result = await client.updateStream('stream-1', { is_active: false });

      expect(result.is_active).toBe(false);
    });

    it('should delete stream', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse(undefined, 204));

      await expect(client.deleteStream('stream-1')).resolves.toBeUndefined();
    });

    it('should test stream', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse({ success: true, latency_ms: 45 }));

      const result = await client.testStream('stream-1');

      expect(result.success).toBe(true);
    });
  });

  describe('alert rules', () => {
    const sampleRule = {
      id: 'rule-1',
      name: 'Failed Login Alert',
      condition: { event_type: 'auth.login.failure', count_threshold: 5, time_window_minutes: 10 },
      notification_channels: [{ type: 'email', config: { to: 'admin@example.com' } }],
      is_active: true,
      cooldown_minutes: 30,
      trigger_count: 0,
      created_at: '2024-01-01T00:00:00Z',
    };

    it('should create alert rule', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse(sampleRule));

      const result = await client.createAlertRule({
        name: 'Failed Login Alert',
        condition: { event_type: 'auth.login.failure', count_threshold: 5, time_window_minutes: 10 },
        notification_channels: [{ type: 'email', config: { to: 'admin@example.com' } }],
      });

      expect(result.id).toBe('rule-1');
    });

    it('should list alert rules', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse({ data: [sampleRule] }));

      const result = await client.listAlertRules();

      expect(result).toHaveLength(1);
    });

    it('should get alert rule', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse(sampleRule));

      const result = await client.getAlertRule('rule-1');

      expect(result.name).toBe('Failed Login Alert');
    });

    it('should throw AlertRuleNotFoundError on 404', async () => {
      fetchMock.mockResolvedValueOnce(mock404('alert_rule_not_found', 'rule-999'));

      await expect(client.getAlertRule('rule-999')).rejects.toThrow(AlertRuleNotFoundError);
    });

    it('should update alert rule', async () => {
      const updated = { ...sampleRule, cooldown_minutes: 60 };
      fetchMock.mockResolvedValueOnce(mockResponse(updated));

      const result = await client.updateAlertRule('rule-1', { cooldown_minutes: 60 });

      expect(result.cooldown_minutes).toBe(60);
    });

    it('should delete alert rule', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse(undefined, 204));

      await expect(client.deleteAlertRule('rule-1')).resolves.toBeUndefined();
    });

    it('should test alert rule', async () => {
      fetchMock.mockResolvedValueOnce(mockResponse({ would_trigger: true, matching_events_count: 7, sample_events: [] }));

      const result = await client.testAlertRule('rule-1');

      expect(result.would_trigger).toBe(true);
    });
  });

  describe('portal', () => {
    it('should generate portal link with ttl', async () => {
      const link = { url: 'https://portal.example.com/audit?token=xyz', expires_at: '2024-01-01T01:00:00Z', organization_id: 'org-1' };
      fetchMock.mockResolvedValueOnce(mockResponse(link));

      const result = await client.generatePortalLink('org-1', 7200);

      expect(result.url).toContain('portal.example.com');
      expect(result.organization_id).toBe('org-1');
    });

    it('should generate portal link without ttl', async () => {
      const link = { url: 'https://portal.example.com/audit?token=abc', expires_at: '2024-01-01T01:00:00Z', organization_id: 'org-1' };
      fetchMock.mockResolvedValueOnce(mockResponse(link));

      const result = await client.generatePortalLink('org-1');

      expect(result.url).toBeDefined();
    });
  });
});
```

2. Add a separate describe block (at file top level) for event constants:

```typescript
import {
  ALL_AUDIT_EVENT_TYPES,
  AUDIT_EVENT_CATEGORIES,
  AUTH_LOGIN_SUCCESS,
  USER_CREATED,
} from '../audit/events';

describe('Audit Event Constants', () => {
  it('should export at least 30 standard event types', () => {
    expect(ALL_AUDIT_EVENT_TYPES.length).toBeGreaterThanOrEqual(30);
  });

  it('should have all categories defined', () => {
    const categories = Object.keys(AUDIT_EVENT_CATEGORIES);
    expect(categories).toContain('auth');
    expect(categories).toContain('user');
    expect(categories).toContain('team');
    expect(categories).toContain('resource');
    expect(categories).toContain('settings');
    expect(categories).toContain('webhook');
    expect(categories).toContain('apikey');
    expect(categories).toContain('audit');
    expect(categories).toContain('system');
  });

  it('should have correct constant values', () => {
    expect(AUTH_LOGIN_SUCCESS).toBe('auth.login.success');
    expect(USER_CREATED).toBe('user.created');
  });
});
```

3. Add error class unit tests:

```typescript
import {
  AuditError,
  AuditEntryNotFoundError,
  IntegrityViolationError,
  IdempotencyConflictError,
} from '../audit/errors';

describe('Audit Errors', () => {
  it('should create AuditError with details', () => {
    const error = new AuditError('test error', { key: 'value' });
    expect(error.message).toBe('test error');
    expect(error.name).toBe('AuditError');
    expect(error.details.key).toBe('value');
  });

  it('should create AuditEntryNotFoundError', () => {
    const error = new AuditEntryNotFoundError('entry-1');
    expect(error.message).toContain('entry-1');
    expect(error.name).toBe('AuditEntryNotFoundError');
    expect(error instanceof AuditError).toBe(true);
  });

  it('should create IntegrityViolationError with optional entryId', () => {
    const withId = new IntegrityViolationError('entry-50');
    expect(withId.message).toContain('entry-50');

    const withoutId = new IntegrityViolationError();
    expect(withoutId.message).toBe('Integrity violation detected');
  });

  it('should create IdempotencyConflictError', () => {
    const error = new IdempotencyConflictError('key-123');
    expect(error.message).toContain('key-123');
    expect(error.name).toBe('IdempotencyConflictError');
  });
});
```

**Tests**: This IS the test code.
**Commit**: `test(audit): add tests for streams, alerts, portal, and event constants`

---

### Task 17: Update Barrel Exports and Package Index

**Files**: modify `packages/node/src/audit/index.ts`, modify `packages/node/src/index.ts`
**Steps**:

1. Replace `src/audit/index.ts` with expanded exports:

```typescript
/**
 * Audit logging module exports.
 */

// Client
export { AuditClient } from './client';
export type { AuditClientOptions, AuditClientConfig } from './client';

// Types
export type {
  // Core models
  AuditSeverity,
  AuditActorType,
  AuditSource,
  AuditEventType,
  GeoLocation,
  AuditActor,
  AuditTarget,
  AuditChange,
  AuditContext,
  AuditLogEntry,
  Pagination,
  AuditLogListResponse,
  // Request/query
  CreateAuditEventRequest,
  BatchAuditResult,
  AuditLogQuery,
  // Event types
  AuditEventTypeDefinition,
  CreateEventTypeRequest,
  UpdateEventTypeRequest,
  // Retention
  RetentionPolicy,
  ArchiveFormat,
  // Export
  ExportFormat,
  ExportDestinationType,
  ExportStatus,
  ExportDestinationConfig,
  ExportRequest,
  ExportResult,
  // Integrity
  IntegrityVerificationResult,
  IntegrityProof,
  // Streaming
  StreamDestinationType,
  StreamFilter,
  AuditStream,
  CreateStreamRequest,
  UpdateStreamRequest,
  StreamTestResult,
  // Alerts
  AlertNotificationChannelType,
  AlertCondition,
  NotificationChannel,
  AlertRule,
  CreateAlertRuleRequest,
  UpdateAlertRuleRequest,
  AlertRuleTestResult,
  // Portal
  PortalLink,
  // Backward-compatible aliases
  ListAuditLogsParams,
  AuditEvent,
} from './types';

// Errors
export {
  AuditError,
  AuditEntryNotFoundError,
  InvalidEventTypeError,
  SchemaValidationError,
  ExportNotFoundError,
  ExportTooLargeError,
  RetentionPolicyError,
  IntegrityViolationError,
  StreamNotFoundError,
  StreamTestError,
  AlertRuleNotFoundError,
  IdempotencyConflictError,
} from './errors';

// Event constants
export {
  // Auth events
  AUTH_LOGIN_SUCCESS,
  AUTH_LOGIN_FAILURE,
  AUTH_LOGOUT,
  AUTH_PASSWORD_CHANGE,
  AUTH_PASSWORD_RESET,
  AUTH_MFA_ENABLED,
  AUTH_MFA_DISABLED,
  AUTH_SESSION_CREATED,
  AUTH_SESSION_REVOKED,
  AUTH_TOKEN_REFRESHED,
  // User events
  USER_CREATED,
  USER_UPDATED,
  USER_DELETED,
  USER_SUSPENDED,
  USER_ACTIVATED,
  USER_ROLE_ASSIGNED,
  USER_ROLE_REMOVED,
  USER_INVITED,
  USER_INVITATION_ACCEPTED,
  // Team events
  TEAM_CREATED,
  TEAM_UPDATED,
  TEAM_DELETED,
  TEAM_MEMBER_ADDED,
  TEAM_MEMBER_REMOVED,
  TEAM_MEMBER_ROLE_CHANGED,
  // Resource events
  RESOURCE_CREATED,
  RESOURCE_UPDATED,
  RESOURCE_DELETED,
  RESOURCE_ACCESSED,
  // Settings events
  SETTINGS_UPDATED,
  SETTINGS_RESET,
  // Webhook events
  WEBHOOK_CREATED,
  WEBHOOK_UPDATED,
  WEBHOOK_DELETED,
  // API Key events
  APIKEY_CREATED,
  APIKEY_REVOKED,
  APIKEY_ROTATED,
  // Audit system events
  AUDIT_EXPORT_REQUESTED,
  AUDIT_EXPORT_COMPLETED,
  AUDIT_INTEGRITY_VIOLATION,
  AUDIT_STREAM_CREATED,
  AUDIT_STREAM_FAILED,
  AUDIT_ALERT_TRIGGERED,
  // System events
  SYSTEM_ERROR,
  SYSTEM_WARNING,
  // Grouped
  AUDIT_EVENT_CATEGORIES,
  ALL_AUDIT_EVENT_TYPES,
} from './events';
```

2. Update the audit section in `src/index.ts` to export the expanded types:

```typescript
// Replace existing audit exports
export { AuditClient } from './audit';

export type {
  // Audit types
  AuditEvent,
  AuditEventType,
  AuditLogEntry,
  AuditLogListResponse,
  AuditLogQuery,
  AuditActor,
  AuditTarget,
  AuditChange,
  AuditContext,
  AuditSeverity,
  CreateAuditEventRequest,
  BatchAuditResult,
  RetentionPolicy,
  ExportRequest,
  ExportResult,
  IntegrityVerificationResult,
  IntegrityProof,
  AuditStream,
  CreateStreamRequest,
  UpdateStreamRequest,
  AlertRule,
  CreateAlertRuleRequest,
  UpdateAlertRuleRequest,
  PortalLink,
} from './audit';

// Re-export key error classes
export {
  AuditError,
  AuditEntryNotFoundError,
  IntegrityViolationError,
} from './audit';

// Re-export event constants
export {
  ALL_AUDIT_EVENT_TYPES,
  AUDIT_EVENT_CATEGORIES,
} from './audit';
```

3. Also add `"./audit"` to the `exports` field in `package.json`:

```json
"./audit": {
  "types": "./dist/audit/index.d.ts",
  "import": "./dist/audit/index.mjs",
  "require": "./dist/audit/index.js"
}
```

**Tests**: Run full test suite to verify no import/export errors.
**Commit**: `feat(audit): update barrel exports and package index`

---

### Task 18: Final Verification and Cleanup

**Files**: all audit module files
**Steps**:

1. Run the full type checker:
   ```bash
   cd packages/node && npm run typecheck
   ```

2. Run all tests:
   ```bash
   cd packages/node && npm test
   ```

3. Run a build to ensure tsup can produce bundles:
   ```bash
   cd packages/node && npm run build
   ```

4. Verify no `any` types slipped in (search for `any` in audit files -- only allowed in `Record<string, unknown>` contexts).

5. Verify backward compatibility: the existing `AuditEvent` and `ListAuditLogsParams` type aliases still work.

6. Remove the `axios` import from `src/audit/client.ts` if it still exists (it should have been replaced in Task 6).

**Tests**: All existing tests plus new audit tests must pass.
**Commit**: `chore(audit): final verification and cleanup`

---

## Summary

| Task | Description | Files | Approx Time |
|------|-------------|-------|-------------|
| 1 | Core model types (Entry, Actor, Target, Change, Context, Query) | `types.ts` | 10 min |
| 2 | Event type, retention, and export types | `types.ts` | 8 min |
| 3 | Integrity, streaming, alert, and portal types | `types.ts` | 10 min |
| 4 | Error classes | `errors.ts` (new) | 8 min |
| 5 | Standard event type constants | `events.ts` (new) | 10 min |
| 6 | Client base infrastructure with fetch and error mapping | `client.ts` | 15 min |
| 7 | Core CRUD: log, logBatch, list, get | `client.ts` | 10 min |
| 8 | Actor and resource query methods | `client.ts` | 5 min |
| 9 | Event type management methods | `client.ts` | 8 min |
| 10 | Retention policy and export methods | `client.ts` | 10 min |
| 11 | Integrity verification methods | `client.ts` | 8 min |
| 12 | SIEM streaming methods | `client.ts` | 12 min |
| 13 | Alert rule and portal methods | `client.ts` | 12 min |
| 14 | Tests: core methods and error handling | `audit.test.ts` (new) | 15 min |
| 15 | Tests: event types, retention, exports, integrity | `audit.test.ts` | 12 min |
| 16 | Tests: streams, alerts, portal, event constants, errors | `audit.test.ts` | 15 min |
| 17 | Barrel exports and package index | `index.ts` files, `package.json` | 10 min |
| 18 | Final verification and cleanup | all | 5 min |

**Total**: 18 tasks, ~173 minutes (~3 hours)

## File Inventory

| File | Action |
|------|--------|
| `packages/node/src/audit/types.ts` | Major rewrite (Tasks 1-3) |
| `packages/node/src/audit/errors.ts` | Create new (Task 4) |
| `packages/node/src/audit/events.ts` | Create new (Task 5) |
| `packages/node/src/audit/client.ts` | Major rewrite (Tasks 6-13) |
| `packages/node/src/audit/index.ts` | Rewrite (Task 17) |
| `packages/node/src/__tests__/audit.test.ts` | Create new (Tasks 14-16) |
| `packages/node/src/index.ts` | Modify audit section (Task 17) |
| `packages/node/package.json` | Add `./audit` export entry (Task 17) |

## API Method Inventory (30 methods total)

| # | Method | HTTP | Path | Task |
|---|--------|------|------|------|
| 1 | `log(event)` | POST | `/api/audit` | 7 |
| 2 | `logBatch(events)` | POST | `/api/audit/batch` | 7 |
| 3 | `list(query?)` | GET | `/api/audit` | 7 |
| 4 | `get(entryId)` | GET | `/api/audit/:id` | 7 |
| 5 | `getByActor(actorId, query?)` | GET | `/api/audit` | 8 |
| 6 | `getByResource(type, id, query?)` | GET | `/api/audit` | 8 |
| 7 | `listEventTypes()` | GET | `/api/audit/event-types` | 9 |
| 8 | `getEventType(name)` | GET | `/api/audit/event-types/:name` | 9 |
| 9 | `createEventType(definition)` | POST | `/api/audit/event-types` | 9 |
| 10 | `updateEventType(name, definition)` | PUT | `/api/audit/event-types/:name` | 9 |
| 11 | `getRetentionPolicy()` | GET | `/api/audit/retention` | 10 |
| 12 | `setRetentionPolicy(policy)` | PUT | `/api/audit/retention` | 10 |
| 13 | `exportLogs(request)` | POST | `/api/audit/exports` | 10 |
| 14 | `getExportStatus(exportId)` | GET | `/api/audit/exports/:id` | 10 |
| 15 | `downloadExport(exportId)` | GET | `/api/audit/exports/:id/download` | 10 |
| 16 | `verifyIntegrity(start, end)` | POST | `/api/audit/integrity/verify` | 11 |
| 17 | `getIntegrityProof(entryId)` | GET | `/api/audit/integrity/:id` | 11 |
| 18 | `createStream(config)` | POST | `/api/audit/streams` | 12 |
| 19 | `listStreams()` | GET | `/api/audit/streams` | 12 |
| 20 | `getStream(streamId)` | GET | `/api/audit/streams/:id` | 12 |
| 21 | `updateStream(streamId, config)` | PUT | `/api/audit/streams/:id` | 12 |
| 22 | `deleteStream(streamId)` | DELETE | `/api/audit/streams/:id` | 12 |
| 23 | `testStream(streamId)` | POST | `/api/audit/streams/:id/test` | 12 |
| 24 | `createAlertRule(rule)` | POST | `/api/audit/alerts` | 13 |
| 25 | `listAlertRules()` | GET | `/api/audit/alerts` | 13 |
| 26 | `getAlertRule(ruleId)` | GET | `/api/audit/alerts/:id` | 13 |
| 27 | `updateAlertRule(ruleId, rule)` | PUT | `/api/audit/alerts/:id` | 13 |
| 28 | `deleteAlertRule(ruleId)` | DELETE | `/api/audit/alerts/:id` | 13 |
| 29 | `testAlertRule(ruleId)` | POST | `/api/audit/alerts/:id/test` | 13 |
| 30 | `generatePortalLink(orgId, ttl?)` | POST | `/api/audit/portal` | 13 |
