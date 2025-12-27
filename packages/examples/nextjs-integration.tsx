/**
 * Next.js Integration Example
 *
 * This example shows how to integrate the shared-platform-sdk
 * into a Next.js frontend application.
 */

'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { AuthClient, UserClient, NotificationClient, UserContext } from '@platform/shared-sdk';

// ============================================================================
// SDK Client Singleton
// ============================================================================

const authClient = new AuthClient({
  issuerUrl: process.env.NEXT_PUBLIC_AUTH_ISSUER_URL || 'https://auth.example.com',
  clientId: process.env.NEXT_PUBLIC_AUTH_CLIENT_ID,
});

const userClient = new UserClient({
  baseUrl: process.env.NEXT_PUBLIC_API_BASE_URL || 'https://api.example.com',
});

const notificationClient = new NotificationClient({
  baseUrl: process.env.NEXT_PUBLIC_API_BASE_URL || 'https://api.example.com',
});

// ============================================================================
// Auth Context
// ============================================================================

interface AuthContextType {
  user: UserContext | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: string) => boolean;
  isAdmin: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserContext | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Check for existing session on mount
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      try {
        const context = authClient.getUserContext(token);
        setUser(context);
        userClient.setAccessToken(token);
        notificationClient.setAccessToken(token);
      } catch {
        // Token expired or invalid
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const tokens = await authClient.login(email, password);

    localStorage.setItem('accessToken', tokens.accessToken);
    if (tokens.refreshToken) {
      localStorage.setItem('refreshToken', tokens.refreshToken);
    }

    const context = authClient.getUserContext(tokens.accessToken);
    setUser(context);
    userClient.setAccessToken(tokens.accessToken);
    notificationClient.setAccessToken(tokens.accessToken);
  };

  const logout = async () => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      try {
        await authClient.logout(token);
      } catch {
        // Ignore logout errors
      }
    }

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
  };

  const hasPermission = (permission: string) => {
    return user?.hasPermission(permission) ?? false;
  };

  const hasRole = (role: string) => {
    return user?.hasRole(role) ?? false;
  };

  const isAdmin = () => {
    return user?.isAdmin() ?? false;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        logout,
        hasPermission,
        hasRole,
        isAdmin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

// ============================================================================
// Permission Components
// ============================================================================

interface PermissionGateProps {
  permission: string;
  children: ReactNode;
  fallback?: ReactNode;
}

export function PermissionGate({ permission, children, fallback = null }: PermissionGateProps) {
  const { hasPermission } = useAuth();

  if (!hasPermission(permission)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}

interface RoleGateProps {
  role: string;
  children: ReactNode;
  fallback?: ReactNode;
}

export function RoleGate({ role, children, fallback = null }: RoleGateProps) {
  const { hasRole } = useAuth();

  if (!hasRole(role)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}

interface AdminGateProps {
  children: ReactNode;
  fallback?: ReactNode;
}

export function AdminGate({ children, fallback = null }: AdminGateProps) {
  const { isAdmin } = useAuth();

  if (!isAdmin()) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}

// ============================================================================
// Hooks
// ============================================================================

export function useUsers() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [pagination, setPagination] = useState<any>(null);

  const fetchUsers = async (params?: { page?: number; pageSize?: number; search?: string }) => {
    setLoading(true);
    setError(null);
    try {
      const result = await userClient.list(params);
      setUsers(result.data);
      setPagination(result.pagination);
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  };

  return { users, loading, error, pagination, fetchUsers };
}

export function useNotifications() {
  const [notifications, setNotifications] = useState<any[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const result = await notificationClient.list();
      setNotifications(result.data);
    } finally {
      setLoading(false);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const result = await notificationClient.getUnreadCount();
      setUnreadCount(result.count);
    } catch {
      // Ignore errors
    }
  };

  const markAsRead = async (id: string) => {
    await notificationClient.markAsRead(id);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
    setUnreadCount((prev) => Math.max(0, prev - 1));
  };

  const markAllAsRead = async () => {
    await notificationClient.markAllAsRead();
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    setUnreadCount(0);
  };

  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();
  }, []);

  return {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
  };
}

// ============================================================================
// Example Components
// ============================================================================

export function LoginForm() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(email, password);
    } catch (err) {
      setError('Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
        required
      />
      {error && <div className="error">{error}</div>}
      <button type="submit" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}

export function UserProfile() {
  const { user, logout } = useAuth();

  if (!user) return null;

  return (
    <div className="user-profile">
      <h2>Welcome, {user.name}!</h2>
      <p>Email: {user.email}</p>
      <p>Roles: {user.roles.join(', ')}</p>
      <button onClick={logout}>Logout</button>
    </div>
  );
}

export function NotificationBell() {
  const { unreadCount, notifications, markAsRead } = useNotifications();
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="notification-bell">
      <button onClick={() => setIsOpen(!isOpen)}>
        Notifications {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
      </button>

      {isOpen && (
        <div className="notification-dropdown">
          {notifications.length === 0 ? (
            <p>No notifications</p>
          ) : (
            notifications.map((notification) => (
              <div
                key={notification.id}
                className={`notification ${notification.read ? 'read' : 'unread'}`}
                onClick={() => markAsRead(notification.id)}
              >
                <strong>{notification.title}</strong>
                <p>{notification.body}</p>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export function UserList() {
  const { users, loading, pagination, fetchUsers } = useUsers();
  const { hasPermission } = useAuth();

  useEffect(() => {
    if (hasPermission('users:read')) {
      fetchUsers();
    }
  }, []);

  if (!hasPermission('users:read')) {
    return <p>You don't have permission to view users.</p>;
  }

  if (loading) {
    return <p>Loading users...</p>;
  }

  return (
    <div>
      <h2>Users ({pagination?.totalItems ?? 0})</h2>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Status</th>
            <th>Roles</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.name}</td>
              <td>{user.email}</td>
              <td>{user.status}</td>
              <td>{user.roles?.join(', ')}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <AdminGate>
        <button>Create New User</button>
      </AdminGate>
    </div>
  );
}

// ============================================================================
// App Layout Example
// ============================================================================

export function AppLayout({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <LoginForm />;
  }

  return (
    <div className="app-layout">
      <header>
        <nav>
          <span>Welcome, {user?.name}</span>
          <NotificationBell />
          <UserProfile />
        </nav>
      </header>

      <main>{children}</main>

      <footer>
        <PermissionGate permission="users:read">
          <a href="/users">Manage Users</a>
        </PermissionGate>

        <AdminGate>
          <a href="/admin">Admin Panel</a>
        </AdminGate>
      </footer>
    </div>
  );
}
