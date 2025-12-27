/**
 * Team types for team/group management.
 */

// Enums as union types
export type TeamMemberRole = 'owner' | 'admin' | 'member';

// User summary for team context
export interface UserSummary {
  id: string;
  email: string;
  name?: string;
  picture?: string;
}

// Team models

export interface Team {
  id?: string;
  tenantId: string;
  name: string;
  slug: string;
  description?: string;
  parentId?: string;
  path?: string;
  level: number;
  ownerId?: string;
  isActive: boolean;
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface TeamSummary {
  id: string;
  name: string;
  slug: string;
  description?: string;
  level: number;
  isActive: boolean;
  memberCount?: number;
}

export interface TeamTree extends Team {
  children: TeamTree[];
  memberCount?: number;
  totalMemberCount?: number;
}

export interface TeamWithDetails extends Team {
  owner?: UserSummary;
  parent?: TeamSummary;
  childrenCount?: number;
  memberCount?: number;
}

export interface TeamMember {
  id?: string;
  teamId: string;
  userId: string;
  role: TeamMemberRole;
  joinedAt?: string;
  invitedBy?: string;
  user?: UserSummary;
}

// Request types

export interface CreateTeamRequest {
  name: string;
  slug: string;
  description?: string;
  parentId?: string;
  ownerId?: string;
  isActive?: boolean;
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;
}

export interface UpdateTeamRequest {
  name?: string;
  description?: string;
  ownerId?: string;
  isActive?: boolean;
  settings?: Record<string, unknown>;
  metadata?: Record<string, unknown>;
}

export interface AddTeamMemberRequest {
  userId: string;
  role?: TeamMemberRole;
}

export interface UpdateTeamMemberRequest {
  role: TeamMemberRole;
}

// Pagination

export interface Pagination {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

export interface TeamListResponse {
  data: TeamSummary[];
  pagination: Pagination;
}

export interface TeamMembersResponse {
  data: TeamMember[];
  pagination: Pagination;
}

// List parameters

export interface ListTeamsParams {
  page?: number;
  pageSize?: number;
  parentId?: string;
  isActive?: boolean;
  search?: string;
  sort?: string;
}

export interface GetTeamTreeParams {
  rootId?: string;
  maxDepth?: number;
  includeMembers?: boolean;
}

export interface ListTeamMembersParams {
  page?: number;
  pageSize?: number;
  role?: TeamMemberRole;
  includeUser?: boolean;
}
