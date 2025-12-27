import { describe, it, expect, vi, beforeEach } from 'vitest';
import jwt from 'jsonwebtoken';
import { AuthClient, UserContext, TokenExpiredError, InvalidTokenError } from '../auth';
import { ROLES, getRolePermissions, checkPermission } from '../auth/roles';

// Helper to create test tokens
function createTestToken(payload: Record<string, any>, options: jwt.SignOptions = {}): string {
  return jwt.sign(payload, 'test-secret', { expiresIn: '1h', ...options });
}

function createExpiredToken(payload: Record<string, any>): string {
  return jwt.sign(payload, 'test-secret', { expiresIn: '-1h' });
}

describe('UserContext', () => {
  const validPayload = {
    sub: 'user-123',
    email: 'test@example.com',
    name: 'Test User',
    tenant_id: 'tenant-456',
    roles: ['user', 'admin'],
    permissions: ['users:read', 'users:write'],
  };

  describe('fromToken', () => {
    it('should create UserContext from valid token', () => {
      const token = createTestToken(validPayload);
      const context = UserContext.fromToken(token);

      expect(context.userId).toBe('user-123');
      expect(context.email).toBe('test@example.com');
      expect(context.name).toBe('Test User');
      expect(context.tenantId).toBe('tenant-456');
      expect(context.roles).toContain('user');
      expect(context.roles).toContain('admin');
    });

    it('should throw TokenExpiredError for expired token', () => {
      const token = createExpiredToken(validPayload);

      expect(() => UserContext.fromToken(token)).toThrow(TokenExpiredError);
    });

    it('should throw InvalidTokenError for invalid token', () => {
      expect(() => UserContext.fromToken('invalid-token')).toThrow(InvalidTokenError);
    });
  });

  describe('hasPermission', () => {
    it('should return true for direct permission', () => {
      const token = createTestToken(validPayload);
      const context = UserContext.fromToken(token);

      expect(context.hasPermission('users:read')).toBe(true);
      expect(context.hasPermission('users:write')).toBe(true);
    });

    it('should return false for missing permission', () => {
      const token = createTestToken(validPayload);
      const context = UserContext.fromToken(token);

      expect(context.hasPermission('settings:read')).toBe(false);
    });

    it('should support wildcard permissions', () => {
      const token = createTestToken({
        ...validPayload,
        permissions: ['users:*'],
      });
      const context = UserContext.fromToken(token);

      expect(context.hasPermission('users:read')).toBe(true);
      expect(context.hasPermission('users:write')).toBe(true);
      expect(context.hasPermission('users:delete')).toBe(true);
    });
  });

  describe('hasRole', () => {
    it('should return true for existing role', () => {
      const token = createTestToken(validPayload);
      const context = UserContext.fromToken(token);

      expect(context.hasRole('user')).toBe(true);
      expect(context.hasRole('admin')).toBe(true);
    });

    it('should return false for missing role', () => {
      const token = createTestToken(validPayload);
      const context = UserContext.fromToken(token);

      expect(context.hasRole('super_admin')).toBe(false);
    });
  });

  describe('isAdmin', () => {
    it('should return true for admin role', () => {
      const token = createTestToken(validPayload);
      const context = UserContext.fromToken(token);

      expect(context.isAdmin()).toBe(true);
    });

    it('should return true for super_admin role', () => {
      const token = createTestToken({
        ...validPayload,
        roles: ['super_admin'],
      });
      const context = UserContext.fromToken(token);

      expect(context.isAdmin()).toBe(true);
    });

    it('should return false for regular user', () => {
      const token = createTestToken({
        ...validPayload,
        roles: ['user'],
      });
      const context = UserContext.fromToken(token);

      expect(context.isAdmin()).toBe(false);
    });
  });
});

describe('Roles', () => {
  describe('getRolePermissions', () => {
    it('should return all permissions for super_admin', () => {
      const perms = getRolePermissions('super_admin');
      expect(perms.has('*')).toBe(true);
    });

    it('should return permissions with inheritance for admin', () => {
      const perms = getRolePermissions('admin');

      // Admin permissions
      expect(perms.has('users:*')).toBe(true);
      expect(perms.has('settings:*')).toBe(true);

      // Inherited from manager
      expect(perms.has('team:*')).toBe(true);

      // Inherited from user
      expect(perms.has('profile:*')).toBe(true);
    });

    it('should return user permissions', () => {
      const perms = getRolePermissions('user');

      expect(perms.has('profile:*')).toBe(true);
      expect(perms.has('notifications:*')).toBe(true);
      expect(perms.has('users:*')).toBe(false);
    });
  });

  describe('checkPermission', () => {
    it('should match exact permissions', () => {
      const granted = new Set(['users:read', 'users:write']);

      expect(checkPermission(granted, 'users:read')).toBe(true);
      expect(checkPermission(granted, 'users:write')).toBe(true);
      expect(checkPermission(granted, 'users:delete')).toBe(false);
    });

    it('should match wildcard permissions', () => {
      const granted = new Set(['users:*', 'reports:read']);

      expect(checkPermission(granted, 'users:read')).toBe(true);
      expect(checkPermission(granted, 'users:write')).toBe(true);
      expect(checkPermission(granted, 'users:delete')).toBe(true);
      expect(checkPermission(granted, 'reports:read')).toBe(true);
      expect(checkPermission(granted, 'reports:write')).toBe(false);
    });

    it('should match super wildcard', () => {
      const granted = new Set(['*']);

      expect(checkPermission(granted, 'users:read')).toBe(true);
      expect(checkPermission(granted, 'settings:write')).toBe(true);
      expect(checkPermission(granted, 'anything:anything')).toBe(true);
    });
  });
});

describe('AuthClient', () => {
  let client: AuthClient;

  beforeEach(() => {
    client = new AuthClient({ issuerUrl: 'https://auth.example.com' });
  });

  describe('getUserContext', () => {
    it('should return UserContext from valid token', () => {
      const token = createTestToken({
        sub: 'user-123',
        email: 'test@example.com',
        name: 'Test User',
        roles: ['user'],
        permissions: [],
      });

      const context = client.getUserContext(token);

      expect(context.userId).toBe('user-123');
      expect(context.email).toBe('test@example.com');
    });

    it('should throw for expired token', () => {
      const token = createExpiredToken({
        sub: 'user-123',
        email: 'test@example.com',
        roles: [],
        permissions: [],
      });

      expect(() => client.getUserContext(token)).toThrow(TokenExpiredError);
    });
  });
});
