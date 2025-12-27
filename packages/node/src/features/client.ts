/**
 * Feature flag client for managing and evaluating feature toggles.
 */

import axios, { AxiosInstance } from 'axios';
import type {
  FeatureFlag,
  FeatureFlagListResponse,
  FeatureFlagEvaluation,
  EvaluationContext,
  ListFeatureFlagsParams,
} from './types';

export interface FeatureFlagClientConfig {
  baseUrl: string;
  accessToken?: string;
  timeout?: number;
  cacheTtl?: number;
}

export class FeatureFlagClient {
  private client: AxiosInstance;
  private accessToken?: string;
  private cache: Map<string, { value: any; expiry: number }> = new Map();
  private cacheTtl: number;

  constructor(config: FeatureFlagClientConfig) {
    this.accessToken = config.accessToken;
    this.cacheTtl = config.cacheTtl || 60000; // 60 seconds default

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
   * List feature flags.
   */
  async list(params?: ListFeatureFlagsParams): Promise<FeatureFlagListResponse> {
    const queryParams: Record<string, string | number | boolean> = {};

    if (params?.page) queryParams.page = params.page;
    if (params?.pageSize) queryParams.page_size = params.pageSize;
    if (params?.tag) queryParams.tag = params.tag;
    if (params?.enabled !== undefined) queryParams.enabled = params.enabled;

    const response = await this.client.get<FeatureFlagListResponse>('/api/features', {
      params: queryParams,
    });
    return response.data;
  }

  /**
   * Get a feature flag by key.
   */
  async get(key: string): Promise<FeatureFlag> {
    const response = await this.client.get<FeatureFlag>(`/api/features/${key}`);
    return response.data;
  }

  /**
   * Evaluate a feature flag for a given context.
   */
  async evaluate(
    key: string,
    context?: EvaluationContext,
    defaultValue: any = false
  ): Promise<FeatureFlagEvaluation> {
    try {
      const response = await this.client.post<FeatureFlagEvaluation>(
        `/api/features/${key}/evaluate`,
        context || {}
      );
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return {
          key,
          enabled: typeof defaultValue === 'boolean' ? defaultValue : false,
          value: defaultValue,
          reason: 'flag_not_found',
        };
      }
      throw error;
    }
  }

  /**
   * Check if a feature flag is enabled for a given context.
   */
  async isEnabled(
    key: string,
    context?: EvaluationContext,
    defaultValue: boolean = false
  ): Promise<boolean> {
    const result = await this.evaluate(key, context, defaultValue);
    return result.enabled;
  }

  /**
   * Get the value of a feature flag for a given context.
   */
  async getValue<T = any>(
    key: string,
    context?: EvaluationContext,
    defaultValue?: T
  ): Promise<T> {
    const result = await this.evaluate(key, context, defaultValue);
    return result.value as T;
  }

  /**
   * Evaluate all feature flags for a given context.
   */
  async evaluateAll(
    context?: EvaluationContext
  ): Promise<Record<string, FeatureFlagEvaluation>> {
    const response = await this.client.post<Record<string, FeatureFlagEvaluation>>(
      '/api/features/evaluate-all',
      context || {}
    );
    return response.data;
  }

  /**
   * Create an evaluation context.
   */
  createContext(options: {
    userId?: string;
    email?: string;
    tenantId?: string;
    roles?: string[];
    [key: string]: any;
  }): EvaluationContext {
    const { userId, email, tenantId, roles, ...attributes } = options;
    return {
      userId,
      email,
      tenantId,
      roles,
      attributes,
    };
  }

  /**
   * Clear the evaluation cache.
   */
  clearCache(): void {
    this.cache.clear();
  }
}
