/**
 * Teams module for team/group management.
 */

export { TeamClient, TeamClientConfig } from './client';

export {
  Team,
  TeamSummary,
  TeamTree,
  TeamWithDetails,
  TeamMember,
  TeamMemberRole,
  UserSummary,
  CreateTeamRequest,
  UpdateTeamRequest,
  AddTeamMemberRequest,
  UpdateTeamMemberRequest,
  TeamListResponse,
  TeamMembersResponse,
  ListTeamsParams,
  GetTeamTreeParams,
  ListTeamMembersParams,
  Pagination,
} from './types';

export {
  TeamError,
  TeamNotFoundError,
  TeamSlugExistsError,
  TeamMemberExistsError,
  TeamMemberNotFoundError,
  TeamHasMembersError,
  TeamHasChildrenError,
  TeamCircularReferenceError,
  TeamOwnerRequiredError,
} from './errors';
