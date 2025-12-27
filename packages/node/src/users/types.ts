/**
 * User type definitions
 */

export type UserStatus = 'active' | 'inactive' | 'pending' | 'suspended' | 'deleted';

export type IdentityProvider = 'local' | 'google' | 'microsoft' | 'okta' | 'saml' | 'oidc';

export interface User {
  id: string;
  email: string;
  emailVerified?: boolean;
  name?: string;
  givenName?: string;
  familyName?: string;
  picture?: string;
  phone?: string;
  phoneVerified?: boolean;
  status: UserStatus;
  roles: string[];
  teamId?: string;
  teamName?: string;
  tenantId?: string;

  // Organization
  departmentId?: string;
  managerId?: string;

  // SSO / External Identity
  identityProvider?: IdentityProvider;
  externalId?: string;
  entraObjectId?: string;
  entraUpn?: string;
  ssoLastSyncAt?: string;

  metadata?: Record<string, any>;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserSummary {
  id: string;
  email: string;
  name?: string;
  picture?: string;
  status: UserStatus;
  roles: string[];
  lastLoginAt?: string;
  createdAt: string;
}

export interface UserProfile {
  id: string;
  email: string;
  emailVerified?: boolean;
  name?: string;
  givenName?: string;
  familyName?: string;
  picture?: string;
  phone?: string;
  phoneVerified?: boolean;
  bio?: string;
  jobTitle?: string;
  department?: string;
  location?: string;
  website?: string;
  socialLinks?: Record<string, string>;
  roles: string[];
  tenantId?: string;
  tenantName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface NotificationPreferencesConfig {
  emailEnabled?: boolean;
  pushEnabled?: boolean;
  smsEnabled?: boolean;
  inAppEnabled?: boolean;
  digestFrequency?: 'realtime' | 'hourly' | 'daily' | 'weekly' | 'never';
  categories?: Record<string, { email?: boolean; push?: boolean; sms?: boolean }>;
}

export interface UserPreferences {
  locale?: string;
  timezone?: string;
  dateFormat?: 'iso' | 'us' | 'eu';
  timeFormat?: '12h' | '24h';
  theme?: 'light' | 'dark' | 'system';
  notifications?: NotificationPreferencesConfig;
}

export interface CreateUserRequest {
  email: string;
  password?: string;
  name?: string;
  givenName?: string;
  familyName?: string;
  phone?: string;
  roles?: string[];
  teamId?: string;
  metadata?: Record<string, any>;
  sendInvitation?: boolean;
}

export interface UpdateUserRequest {
  name?: string;
  givenName?: string;
  familyName?: string;
  phone?: string;
  teamId?: string;
  metadata?: Record<string, any>;
}

export interface InviteUserRequest {
  email: string;
  name?: string;
  roles?: string[];
  teamId?: string;
  message?: string;
  expiresIn?: number;
}

export interface Pagination {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
}

export interface UserListResponse {
  data: User[];
  pagination: Pagination;
}

export interface UserStats {
  totalUsers: number;
  activeUsers: number;
  pendingUsers: number;
  suspendedUsers: number;
  usersByRole?: Record<string, number>;
  usersCreatedLast30Days?: number;
  usersActiveLast7Days?: number;
}
