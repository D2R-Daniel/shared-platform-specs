/**
 * Feature flags type definitions.
 */

export type RuleOperator =
  | 'equals'
  | 'not_equals'
  | 'contains'
  | 'not_contains'
  | 'starts_with'
  | 'ends_with'
  | 'greater_than'
  | 'less_than'
  | 'in'
  | 'not_in'
  | 'matches_regex'
  | 'percentage';

export interface TargetingRule {
  id: string;
  attribute: string;
  operator: RuleOperator;
  value: any;
  enabled: boolean;
}

export interface FeatureFlag {
  id: string;
  key: string;
  name: string;
  description?: string;
  enabled: boolean;
  defaultValue: any;
  targetingRules: TargetingRule[];
  tags: string[];
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
}

export interface Pagination {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
}

export interface FeatureFlagListResponse {
  data: FeatureFlag[];
  pagination: Pagination;
}

export interface FeatureFlagEvaluation {
  key: string;
  enabled: boolean;
  value: any;
  reason: string;
  ruleId?: string;
}

export interface EvaluationContext {
  userId?: string;
  email?: string;
  tenantId?: string;
  roles?: string[];
  attributes?: Record<string, any>;
}

export interface ListFeatureFlagsParams {
  page?: number;
  pageSize?: number;
  tag?: string;
  enabled?: boolean;
}
