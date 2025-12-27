/**
 * Invitation types for user onboarding and team invites.
 */

// Enums as union types
export type InvitationStatus =
  | 'pending'
  | 'sent'
  | 'viewed'
  | 'accepted'
  | 'expired'
  | 'revoked'
  | 'completed';

export type InvitationType =
  | 'user'
  | 'team'
  | 'organization'
  | 'test'
  | 'course'
  | 'custom';

// Invitation models

export interface Invitation {
  id?: string;
  tenantId: string;
  token?: string;
  email: string;
  name?: string;
  invitationType: InvitationType;
  targetId?: string;
  targetRole?: string;
  status: InvitationStatus;
  message?: string;
  expiresAt?: string;
  sentAt?: string;
  viewedAt?: string;
  acceptedAt?: string;
  completedAt?: string;
  metadata?: Record<string, unknown>;
  createdAt?: string;
  createdBy?: string;
}

export interface InvitationSummary {
  id: string;
  email: string;
  name?: string;
  invitationType: InvitationType;
  status: InvitationStatus;
  expiresAt?: string;
  sentAt?: string;
  createdAt?: string;
}

export interface ValidatedInvitation {
  valid: boolean;
  invitation?: Invitation;
  error?: string;
}

// Request types

export interface CreateInvitationRequest {
  email: string;
  name?: string;
  invitationType: InvitationType;
  targetId?: string;
  targetRole?: string;
  message?: string;
  expiresInDays?: number;
  sendEmail?: boolean;
  metadata?: Record<string, unknown>;
}

export interface BulkInvitationRequest {
  invitations: CreateInvitationRequest[];
  sendEmails?: boolean;
}

export interface BulkInvitationFailure {
  email: string;
  reason: string;
}

export interface BulkInvitationResult {
  successful: InvitationSummary[];
  failed: BulkInvitationFailure[];
  total: number;
  successCount: number;
  failureCount: number;
}

export interface AcceptInvitationRequest {
  name?: string;
  password?: string;
  metadata?: Record<string, unknown>;
}

export interface AcceptInvitationResponse {
  success: boolean;
  userId?: string;
  redirectUrl?: string;
  message?: string;
}

export interface ResendInvitationRequest {
  extendExpiry?: boolean;
}

export interface CleanupRequest {
  expirePending?: boolean;
  deleteOlderThanDays?: number;
}

export interface CleanupResult {
  expiredCount: number;
  deletedCount: number;
}

// Pagination

export interface Pagination {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface InvitationListResponse {
  data: InvitationSummary[];
  pagination: Pagination;
}

// List parameters

export interface ListInvitationsParams {
  page?: number;
  pageSize?: number;
  status?: InvitationStatus;
  invitationType?: InvitationType;
  targetId?: string;
  email?: string;
  search?: string;
  sort?: string;
}
