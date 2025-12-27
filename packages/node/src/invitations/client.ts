/**
 * HTTP client for invitation operations.
 */

import axios, { AxiosInstance, AxiosError } from 'axios';
import {
  AcceptInvitationRequest,
  AcceptInvitationResponse,
  BulkInvitationRequest,
  BulkInvitationResult,
  CleanupRequest,
  CleanupResult,
  CreateInvitationRequest,
  Invitation,
  InvitationListResponse,
  ListInvitationsParams,
  ValidatedInvitation,
} from './types';
import {
  ActiveInvitationExistsError,
  InvitationNotFoundError,
  TokenExpiredError,
  TokenNotFoundError,
  TokenRevokedError,
} from './errors';

export interface InvitationClientConfig {
  baseUrl: string;
  accessToken?: string;
  timeout?: number;
}

export class InvitationClient {
  private client: AxiosInstance;

  constructor(config: InvitationClientConfig) {
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

  private handleError(error: unknown, entityId?: string): never {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<{ error?: string; code?: string }>;
      const status = axiosError.response?.status;
      const errorMsg = axiosError.response?.data?.error || '';

      if (status === 404) {
        throw new InvitationNotFoundError(entityId || 'unknown');
      }
      if (status === 409) {
        throw new ActiveInvitationExistsError(entityId || 'unknown');
      }
      if (status === 410) {
        if (errorMsg.toLowerCase().includes('expired')) {
          throw new TokenExpiredError(entityId);
        }
        throw new TokenRevokedError(entityId);
      }
    }
    throw error;
  }

  // Invitation CRUD Operations

  async list(params: ListInvitationsParams = {}): Promise<InvitationListResponse> {
    const response = await this.client.get('/invitations', {
      params: {
        page: params.page || 1,
        page_size: params.pageSize || 20,
        status: params.status,
        invitation_type: params.invitationType,
        target_id: params.targetId,
        email: params.email,
        search: params.search,
        sort: params.sort || 'created_at:desc',
      },
    });
    return response.data;
  }

  async get(invitationId: string): Promise<Invitation> {
    try {
      const response = await this.client.get(`/invitations/${invitationId}`);
      return response.data;
    } catch (error) {
      this.handleError(error, invitationId);
    }
  }

  async create(request: CreateInvitationRequest): Promise<Invitation> {
    try {
      const response = await this.client.post('/invitations', request);
      return response.data;
    } catch (error) {
      this.handleError(error, request.email);
    }
  }

  async createBulk(request: BulkInvitationRequest): Promise<BulkInvitationResult> {
    const response = await this.client.post('/invitations/bulk', request);
    return response.data;
  }

  async revoke(invitationId: string): Promise<void> {
    try {
      await this.client.delete(`/invitations/${invitationId}`);
    } catch (error) {
      this.handleError(error, invitationId);
    }
  }

  async resend(invitationId: string, extendExpiry = true): Promise<Invitation> {
    try {
      const response = await this.client.post(
        `/invitations/${invitationId}/resend`,
        { extend_expiry: extendExpiry }
      );
      return response.data;
    } catch (error) {
      this.handleError(error, invitationId);
    }
  }

  // Public Token Operations

  async validateToken(token: string): Promise<ValidatedInvitation> {
    try {
      const response = await this.client.get(`/invitations/validate/${token}`);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError<{ error?: string }>;
        const status = axiosError.response?.status;
        const errorMsg = axiosError.response?.data?.error || '';

        if (status === 404) {
          throw new TokenNotFoundError(token);
        }
        if (status === 410) {
          if (errorMsg.toLowerCase().includes('expired')) {
            throw new TokenExpiredError(token);
          }
          throw new TokenRevokedError(token);
        }
      }
      throw error;
    }
  }

  async accept(
    token: string,
    request?: AcceptInvitationRequest
  ): Promise<AcceptInvitationResponse> {
    try {
      const response = await this.client.post(
        `/invitations/accept/${token}`,
        request || {}
      );
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError<{ error?: string }>;
        const status = axiosError.response?.status;
        const errorMsg = axiosError.response?.data?.error || '';

        if (status === 404) {
          throw new TokenNotFoundError(token);
        }
        if (status === 410) {
          if (errorMsg.toLowerCase().includes('expired')) {
            throw new TokenExpiredError(token);
          }
          throw new TokenRevokedError(token);
        }
      }
      throw error;
    }
  }

  // Admin Operations

  async cleanup(request?: CleanupRequest): Promise<CleanupResult> {
    const response = await this.client.post('/invitations/cleanup', request || {});
    return response.data;
  }
}
