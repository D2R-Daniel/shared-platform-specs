import type { ComponentType } from 'react';

/** A child navigation link within a parent nav item */
export interface NavChild {
  name: string;
  href: string;
}

/** A top-level navigation item displayed in the sidebar */
export interface NavItem {
  name: string;
  href: string;
  icon: ComponentType<{ className?: string }>;
  children?: NavChild[];
  /** Required permissions to see this nav item (any match) */
  permissions?: string[];
  /** Required roles to see this nav item (any match) */
  roles?: string[];
  /** Section identifier for RBAC canAccess checks */
  section?: string;
}

/** Product-specific role definitions to register with @dream/rbac */
export interface AppCustomRole {
  name: string;
  slug: string;
  level: number;
  permissions: string[];
}

/** Theme overrides for the product */
export interface AppTheme {
  primaryColor?: string;
  accentColor?: string;
}

/** Top-level configuration for a Dream Platform product */
export interface AppConfig {
  /** Product display name (e.g. "Dream Payroll") */
  name: string;
  /** URL-safe product slug (e.g. "payroll") */
  slug: string;
  /** Product icon component displayed in sidebar header */
  icon: ComponentType<{ className?: string }>;
  /** Sidebar navigation items */
  navigation: NavItem[];
  /** Product-specific custom roles */
  customRoles?: AppCustomRole[];
  /** Theme overrides */
  theme?: AppTheme;
}

/**
 * Creates a frozen, validated AppConfig.
 * Call once at app startup and pass to DreamProviders and AppLayout.
 */
export function createAppConfig(config: AppConfig): Readonly<AppConfig> {
  return Object.freeze({ ...config });
}
