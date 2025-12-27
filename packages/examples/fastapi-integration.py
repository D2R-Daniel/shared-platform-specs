"""
FastAPI Integration Example

This example shows how to integrate the shared-platform-sdk
into a FastAPI backend application.
"""

from typing import Optional
from functools import wraps

from fastapi import FastAPI, Depends, HTTPException, status, Query
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel

from shared_platform import AuthClient, UserClient, NotificationClient
from shared_platform.auth import UserContext
from shared_platform.auth.exceptions import TokenExpiredError, InvalidTokenError

# Initialize SDK clients
auth_client = AuthClient(
    issuer_url="https://auth.example.com",
    client_id="your-client-id",
)

user_client = UserClient(base_url="https://api.example.com")
notification_client = NotificationClient(base_url="https://api.example.com")

# Security scheme
security = HTTPBearer()

app = FastAPI(
    title="Platform API",
    description="API using shared-platform-sdk",
    version="1.0.0",
)


# Dependency to get current user context
async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> UserContext:
    """
    Extract and validate user context from JWT token.
    """
    try:
        token = credentials.credentials
        context = auth_client.get_user_context(token)

        # Set token on clients for subsequent requests
        user_client.set_access_token(token)
        notification_client.set_access_token(token)

        return context
    except TokenExpiredError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token has expired",
            headers={"WWW-Authenticate": "Bearer"},
        )
    except InvalidTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token",
            headers={"WWW-Authenticate": "Bearer"},
        )


# Permission checking dependency factory
def require_permission(permission: str):
    """
    Dependency that requires a specific permission.
    """
    async def check_permission(
        context: UserContext = Depends(get_current_user),
    ) -> UserContext:
        if not context.has_permission(permission):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Permission '{permission}' required",
            )
        return context
    return check_permission


# Role checking dependency factory
def require_role(role: str):
    """
    Dependency that requires a specific role.
    """
    async def check_role(
        context: UserContext = Depends(get_current_user),
    ) -> UserContext:
        if not context.has_role(role):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Role '{role}' required",
            )
        return context
    return check_role


# Admin-only dependency
async def require_admin(
    context: UserContext = Depends(get_current_user),
) -> UserContext:
    """
    Dependency that requires admin role.
    """
    if not context.is_admin():
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Admin access required",
        )
    return context


# Request/Response models
class CreateUserRequest(BaseModel):
    email: str
    name: str
    roles: list[str] = ["user"]
    send_invitation: bool = True


class UserResponse(BaseModel):
    id: str
    email: str
    name: str
    status: str
    roles: list[str]


# Routes

@app.get("/health")
async def health_check():
    """Public health check endpoint."""
    return {"status": "ok"}


@app.get("/api/me")
async def get_my_profile(context: UserContext = Depends(get_current_user)):
    """Get current user's profile."""
    profile = await user_client.get_my_profile()
    return profile


@app.get("/api/users")
async def list_users(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    search: Optional[str] = None,
    status: Optional[str] = None,
    context: UserContext = Depends(require_permission("users:read")),
):
    """List users (requires users:read permission)."""
    result = await user_client.list(
        page=page,
        page_size=page_size,
        search=search,
        status=status,
    )
    return result


@app.get("/api/users/{user_id}")
async def get_user(
    user_id: str,
    context: UserContext = Depends(require_permission("users:read")),
):
    """Get a specific user (requires users:read permission)."""
    user = await user_client.get(user_id)
    return user


@app.post("/api/users", status_code=status.HTTP_201_CREATED)
async def create_user(
    request: CreateUserRequest,
    context: UserContext = Depends(require_admin),
):
    """Create a new user (admin only)."""
    from shared_platform.users import CreateUserRequest as SDKCreateUserRequest

    user = await user_client.create(SDKCreateUserRequest(
        email=request.email,
        name=request.name,
        roles=request.roles,
        send_invitation=request.send_invitation,
    ))
    return user


@app.delete("/api/users/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_user(
    user_id: str,
    context: UserContext = Depends(require_admin),
):
    """Delete a user (admin only)."""
    await user_client.delete(user_id)


@app.get("/api/notifications")
async def list_notifications(
    status: Optional[str] = Query(None, description="Filter by status: read, unread"),
    context: UserContext = Depends(get_current_user),
):
    """List current user's notifications."""
    result = await notification_client.list(status=status)
    return result


@app.get("/api/notifications/unread-count")
async def get_unread_count(
    context: UserContext = Depends(get_current_user),
):
    """Get unread notification count."""
    return await notification_client.get_unread_count()


@app.patch("/api/notifications/{notification_id}/read")
async def mark_notification_read(
    notification_id: str,
    context: UserContext = Depends(get_current_user),
):
    """Mark a notification as read."""
    notification = await notification_client.mark_as_read(notification_id)
    return notification


@app.post("/api/notifications/read-all", status_code=status.HTTP_204_NO_CONTENT)
async def mark_all_notifications_read(
    context: UserContext = Depends(get_current_user),
):
    """Mark all notifications as read."""
    await notification_client.mark_all_as_read()


# Run with: uvicorn fastapi_integration:app --reload
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
