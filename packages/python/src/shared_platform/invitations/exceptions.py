"""
Invitation-related exceptions.
"""


class InvitationError(Exception):
    """Base invitation error."""

    def __init__(self, message: str, details: str = ""):
        self.error = message
        self.details = details
        super().__init__(f"{message}: {details}" if details else message)


class InvitationNotFoundError(InvitationError):
    """Invitation not found."""

    def __init__(self, invitation_id: str):
        super().__init__("invitation_not_found", f"Invitation not found: {invitation_id}")
        self.invitation_id = invitation_id


class TokenNotFoundError(InvitationError):
    """Token not found."""

    def __init__(self, token: str = ""):
        super().__init__("token_not_found", "Invalid or unknown token")
        self.token = token


class TokenExpiredError(InvitationError):
    """Token has expired."""

    def __init__(self, token: str = ""):
        super().__init__("token_expired", "Invitation token has expired")
        self.token = token


class TokenRevokedError(InvitationError):
    """Token has been revoked."""

    def __init__(self, token: str = ""):
        super().__init__("token_revoked", "Invitation has been revoked")
        self.token = token


class InvitationAlreadyAcceptedError(InvitationError):
    """Invitation has already been accepted."""

    def __init__(self, invitation_id: str):
        super().__init__(
            "already_accepted",
            f"Invitation {invitation_id} has already been accepted",
        )
        self.invitation_id = invitation_id


class ActiveInvitationExistsError(InvitationError):
    """Active invitation already exists for this email."""

    def __init__(self, email: str):
        super().__init__(
            "active_invitation_exists",
            f"Active invitation already exists for {email}",
        )
        self.email = email


class ResendCooldownError(InvitationError):
    """Cannot resend yet, cooldown period not elapsed."""

    def __init__(self, invitation_id: str, remaining_seconds: int):
        super().__init__(
            "resend_cooldown",
            f"Please wait {remaining_seconds} seconds before resending",
        )
        self.invitation_id = invitation_id
        self.remaining_seconds = remaining_seconds
