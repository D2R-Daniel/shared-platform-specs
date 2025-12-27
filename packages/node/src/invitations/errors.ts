/**
 * Invitation-related errors.
 */

export class InvitationError extends Error {
  public readonly details: Record<string, unknown>;

  constructor(message: string, details: Record<string, unknown> = {}) {
    super(message);
    this.name = 'InvitationError';
    this.details = details;
  }
}

export class InvitationNotFoundError extends InvitationError {
  constructor(invitationId: string) {
    super(`Invitation not found: ${invitationId}`, { invitationId });
    this.name = 'InvitationNotFoundError';
  }
}

export class TokenNotFoundError extends InvitationError {
  constructor(token?: string) {
    super('Invalid or unknown invitation token', token ? { token } : {});
    this.name = 'TokenNotFoundError';
  }
}

export class TokenExpiredError extends InvitationError {
  constructor(token?: string) {
    super('Invitation token has expired', token ? { token } : {});
    this.name = 'TokenExpiredError';
  }
}

export class TokenRevokedError extends InvitationError {
  constructor(token?: string) {
    super('Invitation has been revoked', token ? { token } : {});
    this.name = 'TokenRevokedError';
  }
}

export class InvitationAlreadyAcceptedError extends InvitationError {
  constructor(invitationId: string) {
    super(`Invitation has already been accepted: ${invitationId}`, {
      invitationId,
    });
    this.name = 'InvitationAlreadyAcceptedError';
  }
}

export class ActiveInvitationExistsError extends InvitationError {
  constructor(email: string) {
    super(`Active invitation already exists for: ${email}`, { email });
    this.name = 'ActiveInvitationExistsError';
  }
}

export class ResendCooldownError extends InvitationError {
  constructor(invitationId: string, remainingSeconds: number) {
    super(`Please wait ${remainingSeconds} seconds before resending`, {
      invitationId,
      remainingSeconds,
    });
    this.name = 'ResendCooldownError';
  }
}
