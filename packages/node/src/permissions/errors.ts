/**
 * Permission and role-related errors.
 */

export class RoleError extends Error {
  public readonly details: Record<string, unknown>;

  constructor(message: string, details: Record<string, unknown> = {}) {
    super(message);
    this.name = 'RoleError';
    this.details = details;
  }
}

export class RoleNotFoundError extends RoleError {
  constructor(roleId: string) {
    super(`Role not found: ${roleId}`, { roleId });
    this.name = 'RoleNotFoundError';
  }
}

export class RoleSlugExistsError extends RoleError {
  constructor(slug: string) {
    super(`Role slug already exists: ${slug}`, { slug });
    this.name = 'RoleSlugExistsError';
  }
}

export class SystemRoleError extends RoleError {
  constructor(roleId: string, operation: string) {
    super(`Cannot ${operation} system role: ${roleId}`, { roleId, operation });
    this.name = 'SystemRoleError';
  }
}

export class RoleInUseError extends RoleError {
  constructor(roleId: string, userCount: number) {
    super(`Cannot delete role with ${userCount} assigned users`, {
      roleId,
      userCount,
    });
    this.name = 'RoleInUseError';
  }
}

export class PermissionDeniedError extends RoleError {
  constructor(userId: string, permission: string) {
    super(`Permission denied: ${permission}`, { userId, permission });
    this.name = 'PermissionDeniedError';
  }
}

export class InvalidPermissionFormatError extends RoleError {
  constructor(permission: string) {
    super(`Invalid permission format: ${permission}. Expected "resource:action"`, {
      permission,
    });
    this.name = 'InvalidPermissionFormatError';
  }
}
