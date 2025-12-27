# Integration Examples

This directory contains examples showing how to integrate the shared-platform-sdk into various application types.

## Examples

### Express.js Backend

`express-integration.ts` - Complete Express.js integration with:
- Authentication middleware
- Permission-based route protection
- Role-based access control
- User management endpoints
- Notification endpoints

**Usage:**
```bash
npm install @platform/shared-sdk express
npx ts-node express-integration.ts
```

### FastAPI Backend

`fastapi-integration.py` - Complete FastAPI integration with:
- Dependency injection for authentication
- Permission decorators
- Role-based access control
- User management endpoints
- Notification endpoints

**Usage:**
```bash
pip install shared-platform fastapi uvicorn
uvicorn fastapi_integration:app --reload
```

### Next.js Frontend

`nextjs-integration.tsx` - Complete Next.js integration with:
- Auth context provider
- Permission/Role gate components
- Custom hooks for users and notifications
- Login form component
- User profile component
- Notification bell component

**Usage:**
```tsx
// app/layout.tsx
import { AuthProvider } from './nextjs-integration';

export default function RootLayout({ children }) {
  return (
    <html>
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
```

## Common Patterns

### Authentication Flow

1. User logs in with email/password
2. SDK returns access token and refresh token
3. Access token is stored (localStorage, cookie, etc.)
4. Token is included in all API requests
5. User context is extracted from token for permission checks

### Permission Checking

```typescript
// Check single permission
if (userContext.hasPermission('users:read')) {
  // Can read users
}

// Check wildcard permission
if (userContext.hasPermission('users:*')) {
  // Can do anything with users
}

// Check role
if (userContext.hasRole('admin')) {
  // Is admin
}

// Check admin status
if (userContext.isAdmin()) {
  // Is admin or super_admin
}
```

### Error Handling

```typescript
import { TokenExpiredError, InvalidTokenError, UnauthorizedError } from '@platform/shared-sdk/auth';

try {
  const context = authClient.getUserContext(token);
} catch (error) {
  if (error instanceof TokenExpiredError) {
    // Redirect to login or refresh token
  } else if (error instanceof InvalidTokenError) {
    // Invalid token format
  }
}
```

## Environment Variables

All examples use these environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `AUTH_ISSUER_URL` | Auth server URL | `https://auth.example.com` |
| `AUTH_CLIENT_ID` | OAuth client ID | `my-app` |
| `API_BASE_URL` | API server URL | `https://api.example.com` |

For Next.js, prefix with `NEXT_PUBLIC_`:
- `NEXT_PUBLIC_AUTH_ISSUER_URL`
- `NEXT_PUBLIC_AUTH_CLIENT_ID`
- `NEXT_PUBLIC_API_BASE_URL`
