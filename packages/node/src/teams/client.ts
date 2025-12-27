/**
 * HTTP client for team operations.
 */

import axios, { AxiosInstance, AxiosError } from 'axios';
import {
  AddTeamMemberRequest,
  CreateTeamRequest,
  GetTeamTreeParams,
  ListTeamMembersParams,
  ListTeamsParams,
  Team,
  TeamListResponse,
  TeamMember,
  TeamMembersResponse,
  TeamTree,
  TeamWithDetails,
  UpdateTeamMemberRequest,
  UpdateTeamRequest,
} from './types';
import {
  TeamNotFoundError,
  TeamSlugExistsError,
  TeamMemberExistsError,
  TeamMemberNotFoundError,
  TeamHasMembersError,
  TeamHasChildrenError,
  TeamCircularReferenceError,
} from './errors';

export interface TeamClientConfig {
  baseUrl: string;
  accessToken?: string;
  timeout?: number;
}

export class TeamClient {
  private client: AxiosInstance;

  constructor(config: TeamClientConfig) {
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

  private handleError(error: unknown, entityId?: string, userId?: string): never {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<{ error?: string; code?: string }>;
      const status = axiosError.response?.status;
      const errorCode = axiosError.response?.data?.code;

      if (status === 404) {
        if (errorCode === 'member_not_found' && entityId && userId) {
          throw new TeamMemberNotFoundError(entityId, userId);
        }
        throw new TeamNotFoundError(entityId || 'unknown');
      }
      if (status === 409) {
        if (errorCode === 'slug_exists') {
          throw new TeamSlugExistsError(entityId || 'unknown');
        }
        if (errorCode === 'member_exists' && entityId && userId) {
          throw new TeamMemberExistsError(entityId, userId);
        }
        if (errorCode === 'has_members') {
          throw new TeamHasMembersError(entityId || 'unknown', 0);
        }
        if (errorCode === 'has_children') {
          throw new TeamHasChildrenError(entityId || 'unknown', 0);
        }
        if (errorCode === 'circular_reference') {
          throw new TeamCircularReferenceError(entityId || 'unknown', '');
        }
      }
    }
    throw error;
  }

  // Team CRUD Operations

  async list(params: ListTeamsParams = {}): Promise<TeamListResponse> {
    const response = await this.client.get('/teams', {
      params: {
        page: params.page || 1,
        page_size: params.pageSize || 20,
        parent_id: params.parentId,
        is_active: params.isActive,
        search: params.search,
        sort: params.sort || 'name:asc',
      },
    });
    return response.data;
  }

  async getTree(params: GetTeamTreeParams = {}): Promise<TeamTree[]> {
    const response = await this.client.get('/teams/tree', {
      params: {
        root_id: params.rootId,
        max_depth: params.maxDepth || 10,
        include_members: params.includeMembers || false,
      },
    });
    return response.data.data || response.data;
  }

  async get(
    teamId: string,
    options: { includeOwner?: boolean; includeParent?: boolean } = {}
  ): Promise<TeamWithDetails> {
    try {
      const response = await this.client.get(`/teams/${teamId}`, {
        params: {
          include_owner: options.includeOwner || false,
          include_parent: options.includeParent || false,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(error, teamId);
    }
  }

  async getBySlug(slug: string): Promise<Team> {
    try {
      const response = await this.client.get(`/teams/slug/${slug}`);
      return response.data;
    } catch (error) {
      this.handleError(error, slug);
    }
  }

  async create(request: CreateTeamRequest): Promise<Team> {
    try {
      const response = await this.client.post('/teams', request);
      return response.data;
    } catch (error) {
      this.handleError(error, request.slug);
    }
  }

  async update(teamId: string, request: UpdateTeamRequest): Promise<Team> {
    try {
      const response = await this.client.put(`/teams/${teamId}`, request);
      return response.data;
    } catch (error) {
      this.handleError(error, teamId);
    }
  }

  async delete(teamId: string, force = false): Promise<void> {
    try {
      await this.client.delete(`/teams/${teamId}`, {
        params: force ? { force: true } : {},
      });
    } catch (error) {
      this.handleError(error, teamId);
    }
  }

  async move(teamId: string, newParentId?: string): Promise<Team> {
    try {
      const response = await this.client.post(`/teams/${teamId}/move`, {
        new_parent_id: newParentId,
      });
      return response.data;
    } catch (error) {
      this.handleError(error, teamId);
    }
  }

  // Team Member Operations

  async listMembers(
    teamId: string,
    params: ListTeamMembersParams = {}
  ): Promise<TeamMembersResponse> {
    try {
      const response = await this.client.get(`/teams/${teamId}/members`, {
        params: {
          page: params.page || 1,
          page_size: params.pageSize || 20,
          role: params.role,
          include_user: params.includeUser ?? true,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(error, teamId);
    }
  }

  async addMember(teamId: string, request: AddTeamMemberRequest): Promise<TeamMember> {
    try {
      const response = await this.client.post(`/teams/${teamId}/members`, request);
      return response.data;
    } catch (error) {
      this.handleError(error, teamId, request.userId);
    }
  }

  async updateMember(
    teamId: string,
    userId: string,
    request: UpdateTeamMemberRequest
  ): Promise<TeamMember> {
    try {
      const response = await this.client.put(
        `/teams/${teamId}/members/${userId}`,
        request
      );
      return response.data;
    } catch (error) {
      this.handleError(error, teamId, userId);
    }
  }

  async removeMember(teamId: string, userId: string): Promise<void> {
    try {
      await this.client.delete(`/teams/${teamId}/members/${userId}`);
    } catch (error) {
      this.handleError(error, teamId, userId);
    }
  }
}
