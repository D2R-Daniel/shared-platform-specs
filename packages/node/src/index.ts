/**
 * Shared Platform SDK
 *
 * A unified SDK for authentication, user management, notifications,
 * audit logging, and feature flags across all platform services.
 *
 * @example
 * ```typescript
 * import { AuthClient, UserClient, NotificationClient } from '@platform/shared-sdk';
 * import { AuditClient, FeatureFlagClient } from '@platform/shared-sdk';
 *
 * const auth = new AuthClient({ issuerUrl: 'https://auth.example.com' });
 * const users = new UserClient({ baseUrl: 'https://api.example.com' });
 * ```
 */

export { AuthClient } from './auth';
export { UserClient } from './users';
export { NotificationClient } from './notifications';
export { AuditClient } from './audit';
export { FeatureFlagClient } from './features';

export type {
  // Auth types
  TokenResponse,
  TokenIntrospection,
  UserInfo,
  UserContext,
  Session,
  Role,
  Permission,
} from './auth';

export type {
  // User types
  User,
  UserSummary,
  UserProfile,
  UserPreferences,
  UserStatus,
  CreateUserRequest,
  UpdateUserRequest,
  InviteUserRequest,
  UserListResponse,
} from './users';

export type {
  // Notification types
  Notification,
  NotificationPreferences,
  NotificationCategory,
  ChannelSubscription,
  RegisteredDevice,
  EmailNotificationEvent,
  SMSNotificationEvent,
  PushNotificationEvent,
} from './notifications';

export type {
  // Audit types
  AuditEvent,
  AuditEventType,
  AuditLogEntry,
  AuditLogListResponse,
} from './audit';

export type {
  // Feature flag types
  FeatureFlag,
  FeatureFlagEvaluation,
  EvaluationContext,
  TargetingRule,
} from './features';

export const VERSION = '0.1.0';
