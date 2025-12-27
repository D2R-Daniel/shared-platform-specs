import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios from 'axios';
import { UserClient } from '../users';
import type { User, CreateUserRequest, UpdateUserRequest } from '../users';

// Create mock functions
const mockGet = vi.fn();
const mockPost = vi.fn();
const mockPut = vi.fn();
const mockPatch = vi.fn();
const mockDelete = vi.fn();

// Mock axios.create to return our mock instance
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      get: mockGet,
      post: mockPost,
      put: mockPut,
      patch: mockPatch,
      delete: mockDelete,
      defaults: { headers: { common: {} } },
    })),
  },
}));

describe('UserClient', () => {
  let client: UserClient;

  const sampleUser: User = {
    id: 'user-123',
    email: 'test@example.com',
    name: 'Test User',
    status: 'active',
    roles: ['user'],
    tenantId: 'tenant-456',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    client = new UserClient({
      baseUrl: 'https://api.example.com',
      accessToken: 'test-token',
    });
  });

  describe('list', () => {
    it('should list users with pagination', async () => {
      const mockResponse = {
        data: {
          data: [sampleUser],
          pagination: {
            page: 1,
            pageSize: 20,
            totalItems: 1,
            totalPages: 1,
          },
        },
      };

      mockGet.mockResolvedValueOnce(mockResponse);

      const result = await client.list({ page: 1, pageSize: 20 });

      expect(result.data).toHaveLength(1);
      expect(result.data[0].id).toBe('user-123');
      expect(result.pagination.totalItems).toBe(1);
    });

    it('should list users with search filter', async () => {
      const mockResponse = {
        data: {
          data: [sampleUser],
          pagination: {
            page: 1,
            pageSize: 20,
            totalItems: 1,
            totalPages: 1,
          },
        },
      };

      mockGet.mockResolvedValueOnce(mockResponse);

      await client.list({ search: 'test' });

      expect(mockGet).toHaveBeenCalledWith('/users', {
        params: expect.objectContaining({ search: 'test' }),
      });
    });
  });

  describe('get', () => {
    it('should get a single user', async () => {
      mockGet.mockResolvedValueOnce({ data: sampleUser });

      const user = await client.get('user-123');

      expect(user.id).toBe('user-123');
      expect(user.email).toBe('test@example.com');
      expect(mockGet).toHaveBeenCalledWith('/users/user-123');
    });
  });

  describe('create', () => {
    it('should create a new user', async () => {
      mockPost.mockResolvedValueOnce({ data: sampleUser });

      const request: CreateUserRequest = {
        email: 'test@example.com',
        name: 'Test User',
        roles: ['user'],
      };

      const user = await client.create(request);

      expect(user.id).toBe('user-123');
      expect(mockPost).toHaveBeenCalledWith('/users', request);
    });

    it('should create user with invitation', async () => {
      mockPost.mockResolvedValueOnce({ data: sampleUser });

      const request: CreateUserRequest = {
        email: 'test@example.com',
        name: 'Test User',
        roles: ['user'],
        sendInvitation: true,
      };

      await client.create(request);

      expect(mockPost).toHaveBeenCalledWith('/users', expect.objectContaining({ sendInvitation: true }));
    });
  });

  describe('update', () => {
    it('should update a user', async () => {
      const updatedUser = { ...sampleUser, name: 'Updated Name' };
      mockPut.mockResolvedValueOnce({ data: updatedUser });

      const request: UpdateUserRequest = { name: 'Updated Name' };

      const user = await client.update('user-123', request);

      expect(user.name).toBe('Updated Name');
      expect(mockPut).toHaveBeenCalledWith('/users/user-123', request);
    });
  });

  describe('delete', () => {
    it('should delete a user', async () => {
      mockDelete.mockResolvedValueOnce({ data: {} });

      await client.delete('user-123');

      expect(mockDelete).toHaveBeenCalledWith('/users/user-123');
    });
  });

  describe('updateStatus', () => {
    it('should update user status', async () => {
      const suspendedUser = { ...sampleUser, status: 'suspended' };
      mockPatch.mockResolvedValueOnce({ data: suspendedUser });

      const user = await client.updateStatus('user-123', 'suspended', 'Policy violation');

      expect(user.status).toBe('suspended');
      expect(mockPatch).toHaveBeenCalledWith('/users/user-123/status', {
        status: 'suspended',
        reason: 'Policy violation',
      });
    });
  });

  describe('updateRoles', () => {
    it('should update user roles', async () => {
      const updatedUser = { ...sampleUser, roles: ['admin', 'user'] };
      mockPut.mockResolvedValueOnce({ data: updatedUser });

      const user = await client.updateRoles('user-123', ['admin', 'user']);

      expect(user.roles).toContain('admin');
      expect(mockPut).toHaveBeenCalledWith('/users/user-123/roles', { roles: ['admin', 'user'] });
    });
  });

  describe('resetPassword', () => {
    it('should reset user password', async () => {
      mockPost.mockResolvedValueOnce({ data: { message: 'Password reset email sent' } });

      const result = await client.resetPassword('user-123', true);

      expect(result.message).toBe('Password reset email sent');
      expect(mockPost).toHaveBeenCalledWith('/users/user-123/password', { send_email: true });
    });
  });

  describe('profile operations', () => {
    it('should get my profile', async () => {
      const profile = {
        id: 'user-123',
        email: 'test@example.com',
        name: 'Test User',
      };
      mockGet.mockResolvedValueOnce({ data: profile });

      const result = await client.getMyProfile();

      expect(result.id).toBe('user-123');
      expect(mockGet).toHaveBeenCalledWith('/me');
    });

    it('should change password', async () => {
      mockPost.mockResolvedValueOnce({ data: {} });

      await client.changePassword('oldPassword', 'newPassword');

      expect(mockPost).toHaveBeenCalledWith('/me/password', {
        current_password: 'oldPassword',
        new_password: 'newPassword',
      });
    });
  });

  describe('getStats', () => {
    it('should get user stats', async () => {
      const stats = {
        totalUsers: 100,
        activeUsers: 80,
        pendingUsers: 15,
        suspendedUsers: 5,
      };
      mockGet.mockResolvedValueOnce({ data: stats });

      const result = await client.getStats();

      expect(result.totalUsers).toBe(100);
      expect(mockGet).toHaveBeenCalledWith('/users/stats');
    });
  });
});
