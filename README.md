# Shared Platform SDK

A unified SDK monorepo for authentication, user management, and notifications across all platform services.

## Architecture

```
shared-platform-sdk/
├── openapi/          # REST API specifications (OpenAPI 3.0)
├── events/           # Event schemas (Avro)
├── models/           # Shared data models
└── packages/         # Language-specific SDKs
    ├── python/       # PyPI: shared-platform
    ├── node/         # npm: @platform/shared-sdk
    └── java/         # Maven: com.platform:shared-sdk
```

## Installation

### Python
```bash
pip install shared-platform
```

### Node.js
```bash
npm install @platform/shared-sdk
# or
yarn add @platform/shared-sdk
```

### Java (Maven)
```xml
<dependency>
    <groupId>com.platform</groupId>
    <artifactId>shared-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Java (Gradle)
```groovy
implementation 'com.platform:shared-sdk:0.1.0'
```

## Quick Start

### Python
```python
from shared_platform import AuthClient, UserClient, NotificationClient

# Initialize clients
auth = AuthClient(issuer_url="https://auth.example.com")
users = UserClient(base_url="https://api.example.com")
notifications = NotificationClient(base_url="https://api.example.com")

# Authenticate
tokens = auth.login("user@example.com", "password")

# Get user context from token
context = auth.get_user_context(tokens.access_token)
print(f"Hello, {context.name}!")

# Check permissions
if context.has_permission("users:read"):
    user_list = users.list(page=1, page_size=20)
    print(f"Found {user_list.pagination.total_items} users")
```

### Node.js/TypeScript
```typescript
import { AuthClient, UserClient, NotificationClient } from '@platform/shared-sdk';

// Initialize clients
const auth = new AuthClient({ issuerUrl: 'https://auth.example.com' });
const users = new UserClient({ baseUrl: 'https://api.example.com' });
const notifications = new NotificationClient({ baseUrl: 'https://api.example.com' });

// Authenticate
const tokens = await auth.login('user@example.com', 'password');

// Get user context from token
const context = auth.getUserContext(tokens.accessToken);
console.log(`Hello, ${context.name}!`);

// Check permissions
if (context.hasPermission('users:read')) {
  const userList = await users.list({ page: 1, pageSize: 20 });
  console.log(`Found ${userList.pagination.totalItems} users`);
}
```

### Java
```java
import com.platform.sdk.auth.AuthClient;
import com.platform.sdk.auth.UserContext;
import com.platform.sdk.users.UserClient;

// Initialize clients
AuthClient auth = new AuthClient.Builder()
    .issuerUrl("https://auth.example.com")
    .build();

UserClient users = new UserClient.Builder()
    .baseUrl("https://api.example.com")
    .build();

// Authenticate
TokenResponse tokens = auth.login("user@example.com", "password");

// Get user context from token
UserContext context = auth.getUserContext(tokens.getAccessToken());
System.out.println("Hello, " + context.getName());

// Check permissions
if (context.hasPermission("users:read")) {
    UserListResponse result = users.list(new ListUsersParams().page(1).pageSize(20));
    System.out.println("Found " + result.getPagination().getTotalItems() + " users");
}
```

## Modules

### Authentication
- OAuth2/OIDC login, logout, token refresh
- JWT token validation and introspection
- User context extraction from tokens
- Role and permission checking

### User Management
- User CRUD operations
- Profile and preferences management
- Password changes
- Status updates (active, suspended, etc.)

### Notifications
- In-app notification management
- Mark as read/unread
- Notification preferences
- Device registration for push notifications
- Event types for email, SMS, and push

## Roles & Permissions

Built-in role hierarchy with permission inheritance:

| Role | Inherits | Key Permissions |
|------|----------|-----------------|
| `super_admin` | admin | `*` (all) |
| `admin` | manager | `users:*`, `settings:*` |
| `manager` | user | `reports:*`, `team:*` |
| `user` | guest | `profile:*`, `notifications:*` |
| `guest` | - | `*.read` (read-only) |

```python
# Python
from shared_platform.auth import get_role_permissions, check_permission

perms = get_role_permissions("admin")  # Returns all permissions including inherited

granted = ["users:*", "reports:read"]
check_permission(granted, "users:create")  # True (wildcard match)
check_permission(granted, "settings:read")  # False
```

## Development

### Building Packages

```bash
# Build all packages
make build-all

# Build specific package
make build-python
make build-node
make build-java
```

### Running Tests

```bash
# Run all tests
make test-all

# Run specific tests
make test-python
make test-node
make test-java
```

### Validating Specs

```bash
# Validate OpenAPI specs
make validate

# Generate API docs
make docs
```

### Publishing

```bash
# Publish all packages
make publish-all

# Publish specific package
make publish-python   # to PyPI
make publish-node     # to npm
make publish-java     # to Maven Central
```

## Repository Structure

```
shared-platform-sdk/
├── openapi/                    # REST API contracts (OpenAPI 3.0)
│   ├── auth/                   # Authentication endpoints
│   ├── users/                  # User management endpoints
│   └── notifications/          # Notification preferences
├── events/                     # Event schemas (Avro)
│   └── notifications/          # Email, SMS, Push events
├── models/                     # Shared data models
│   ├── auth/                   # Roles, permissions, user context
│   ├── users/                  # User, profile, validation rules
│   └── notifications/          # Templates, preferences
├── packages/                   # Language-specific SDKs
│   ├── python/                 # Python package (PyPI)
│   ├── node/                   # Node.js package (npm)
│   └── java/                   # Java package (Maven)
├── scripts/                    # Build and utility scripts
├── Makefile                    # Build automation
└── VERSION                     # Current version
```

## Versioning

This repository follows [Semantic Versioning](https://semver.org/):
- **MAJOR**: Breaking changes to SDK APIs
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes and documentation

All packages share the same version number defined in `VERSION`.

## License

MIT
