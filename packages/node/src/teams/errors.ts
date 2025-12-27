/**
 * Team-related errors.
 */

export class TeamError extends Error {
  public readonly details: Record<string, unknown>;

  constructor(message: string, details: Record<string, unknown> = {}) {
    super(message);
    this.name = 'TeamError';
    this.details = details;
  }
}

export class TeamNotFoundError extends TeamError {
  constructor(teamId: string) {
    super(`Team not found: ${teamId}`, { teamId });
    this.name = 'TeamNotFoundError';
  }
}

export class TeamSlugExistsError extends TeamError {
  constructor(slug: string) {
    super(`Team slug already exists: ${slug}`, { slug });
    this.name = 'TeamSlugExistsError';
  }
}

export class TeamMemberExistsError extends TeamError {
  constructor(teamId: string, userId: string) {
    super(`User is already a member of this team`, { teamId, userId });
    this.name = 'TeamMemberExistsError';
  }
}

export class TeamMemberNotFoundError extends TeamError {
  constructor(teamId: string, userId: string) {
    super(`User is not a member of this team`, { teamId, userId });
    this.name = 'TeamMemberNotFoundError';
  }
}

export class TeamHasMembersError extends TeamError {
  constructor(teamId: string, memberCount: number) {
    super(`Cannot delete team with ${memberCount} members`, {
      teamId,
      memberCount,
    });
    this.name = 'TeamHasMembersError';
  }
}

export class TeamHasChildrenError extends TeamError {
  constructor(teamId: string, childrenCount: number) {
    super(`Cannot delete team with ${childrenCount} child teams`, {
      teamId,
      childrenCount,
    });
    this.name = 'TeamHasChildrenError';
  }
}

export class TeamCircularReferenceError extends TeamError {
  constructor(teamId: string, newParentId: string) {
    super('Cannot move team: would create circular reference', {
      teamId,
      newParentId,
    });
    this.name = 'TeamCircularReferenceError';
  }
}

export class TeamOwnerRequiredError extends TeamError {
  constructor(teamId: string) {
    super('Team must have at least one owner', { teamId });
    this.name = 'TeamOwnerRequiredError';
  }
}
