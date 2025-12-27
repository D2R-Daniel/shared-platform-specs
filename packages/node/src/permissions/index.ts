/**
 * Permissions module for role-based access control.
 */

export {
  RoleClient,
  RoleClientConfig,
  matchesPermission,
  hasAnyPermission,
  hasAllPermissions,
} from './client';

export {
  Role,
  RoleSummary,
  RoleAssignment,
  UserRole,
  Permission,
  PermissionCheckResult,
  CreateRoleRequest,
  UpdateRoleRequest,
  AssignRoleRequest,
  PermissionCheckRequest,
  RoleListResponse,
  UserRolesResponse,
  ListRolesParams,
  Pagination,
} from './types';

export {
  RoleError,
  RoleNotFoundError,
  RoleSlugExistsError,
  SystemRoleError,
  RoleInUseError,
  PermissionDeniedError,
  InvalidPermissionFormatError,
} from './errors';
