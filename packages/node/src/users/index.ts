/**
 * User management module
 */

export { UserClient } from './client';
export type { UserClientOptions } from './client';

export type {
  User,
  UserSummary,
  UserProfile,
  UserPreferences,
  UserStatus,
  IdentityProvider,
  CreateUserRequest,
  UpdateUserRequest,
  InviteUserRequest,
  UserListResponse,
  Pagination,
} from './types';
