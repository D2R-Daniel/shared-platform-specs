/**
 * Tenant-related errors.
 */

export class TenantError extends Error {
  public readonly details: Record<string, unknown>;

  constructor(message: string, details: Record<string, unknown> = {}) {
    super(message);
    this.name = 'TenantError';
    this.details = details;
  }
}

export class TenantNotFoundError extends TenantError {
  constructor(tenantId: string) {
    super(`Tenant not found: ${tenantId}`, { tenantId });
    this.name = 'TenantNotFoundError';
  }
}

export class TenantSlugExistsError extends TenantError {
  constructor(slug: string) {
    super(`Tenant slug already exists: ${slug}`, { slug });
    this.name = 'TenantSlugExistsError';
  }
}

export class TenantDomainExistsError extends TenantError {
  constructor(domain: string) {
    super(`Tenant domain already exists: ${domain}`, { domain });
    this.name = 'TenantDomainExistsError';
  }
}

export class SSOConfigError extends TenantError {
  constructor(message: string, details: Record<string, unknown> = {}) {
    super(message, details);
    this.name = 'SSOConfigError';
  }
}

export class SSOConfigNotFoundError extends SSOConfigError {
  constructor(tenantId: string) {
    super(`SSO configuration not found for tenant: ${tenantId}`, { tenantId });
    this.name = 'SSOConfigNotFoundError';
  }
}

export class SSOConnectionError extends SSOConfigError {
  constructor(message: string, provider?: string) {
    super(message, provider ? { provider } : {});
    this.name = 'SSOConnectionError';
  }
}

export class SSOProviderError extends SSOConfigError {
  constructor(provider: string, message: string) {
    super(`SSO provider error (${provider}): ${message}`, { provider });
    this.name = 'SSOProviderError';
  }
}

export class DepartmentError extends TenantError {
  constructor(message: string, details: Record<string, unknown> = {}) {
    super(message, details);
    this.name = 'DepartmentError';
  }
}

export class DepartmentNotFoundError extends DepartmentError {
  constructor(departmentId: string) {
    super(`Department not found: ${departmentId}`, { departmentId });
    this.name = 'DepartmentNotFoundError';
  }
}

export class DepartmentCodeExistsError extends DepartmentError {
  constructor(code: string, tenantId: string) {
    super(`Department code already exists: ${code}`, { code, tenantId });
    this.name = 'DepartmentCodeExistsError';
  }
}

export class DepartmentHasMembersError extends DepartmentError {
  constructor(departmentId: string, memberCount: number) {
    super(`Cannot delete department with ${memberCount} members`, {
      departmentId,
      memberCount,
    });
    this.name = 'DepartmentHasMembersError';
  }
}

export class DepartmentHasChildrenError extends DepartmentError {
  constructor(departmentId: string, childrenCount: number) {
    super(`Cannot delete department with ${childrenCount} child departments`, {
      departmentId,
      childrenCount,
    });
    this.name = 'DepartmentHasChildrenError';
  }
}

export class DepartmentCircularReferenceError extends DepartmentError {
  constructor(departmentId: string, newParentId: string) {
    super('Cannot move department: would create circular reference', {
      departmentId,
      newParentId,
    });
    this.name = 'DepartmentCircularReferenceError';
  }
}
