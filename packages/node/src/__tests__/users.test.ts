import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { UserClient } from '../users';
import type { User, CreateUserRequest, UpdateUserRequest } from '../users';

// Mock axios
vi.mock('axios');
const mockedAxios = vi.mocked(axios);

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

    // Mock axios.create to return our mocked axios
    mockedAxios.create = vi.fn().mockReturnValue({
      get: mockedAxios.get,
      post: mockedAxios.post,
      patch: mockedAxios.patch,
      delete: mockedAxios.delete,
      defaults: { headers: { common: {} } },
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

      mockedAxios.get.mockResolvedValueOnce(mockResponse);

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

      mockedAxios.get.mockResolvedValueOnce(mockResponse);

      const result = await client.list({ search: 'test' });

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/users',
        expect.objectContaining({
          params: expect.objectContaining({ search: 'test' }),
        })
      );
    });
  });

  describe('get', () => {
    it('should get a single user', async () => {
      mockedAxios.get.mockResolvedValueOnce({ data: sampleUser });

      const user = await client.get('user-123');

      expect(user.id).toBe('user-123');
      expect(user.email).toBe('test@example.com');
    });
  });

  describe('create', () => {
    it('should create a new user', async () => {
      mockedAxios.post.mockResolvedValueOnce({ data: sampleUser });

      const request: CreateUserRequest = {
        email: 'test@example.com',
        name: 'Test User',
        roles: ['user'],
      };

      const user = await client.create(request);

      expect(user.id).toBe('user-123');
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/users', request);
    });

    it('should create user with invitation', async () => {
      mockedAxios.post.mockResolvedValueOnce({ data: sampleUser });

      const request: CreateUserRequest = {
        email: 'test@example.com',
        name: 'Test User',
        roles: ['user'],
        sendInvitation: true,
      };

      await client.create(request);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/users',
        expect.objectContaining({ sendInvitation: true })
      );
    });
  });

  describe('update', () => {
    it('should update a user', async () => {
      const updatedUser = { ...sampleUser, name: 'Updated Name' };
      mockedAxios.patch.mockResolvedValueOnce({ data: updatedUser });

      const request: UpdateUserRequest = { name: 'Updated Name' };

      const user = await client.update('user-123', request);

      expect(user.name).toBe('Updated Name');
      expect(mockedAxios.patch).toHaveBeenCalledWith('/api/users/user-123', request);
    });
  });

  describe('delete', () => {
    it('should delete a user', async () => {
      mockedAxios.delete.mockResolvedValueOnce({ data: {} });

      await client.delete('user-123');

      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/users/user-123');
    });
  });

  describe('updateStatus', () => {
    it('should update user status', async () => {
      const suspendedUser = { ...sampleUser, status: 'suspended' };
      mockedAxios.patch.mockResolvedValueOnce({ data: suspendedUser });

      const user = await client.updateStatus('user-123', 'suspended', 'Policy violation');

      expect(user.status).toBe('suspended');
      expect(mockedAxios.patch).toHaveBeenCalledWith(
        '/api/users/user-123/status',
        { status: 'suspended', reason: 'Policy violation' }
      );
    });
  });

  describe('profile operations', () => {
    it('should get my profile', async () => {
      const profile = {
        id: 'user-123',
        email: 'test@example.com',
        name: 'Test User',
      };
      mockedAxios.get.mockResolvedValueOnce({ data: profile });

      const result = await client.getMyProfile();

      expect(result.id).toBe('user-123');
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/users/me');
    });

    it('should change password', async () => {
      mockedAxios.post.mockResolvedValueOnce({ data: {} });

      await client.changePassword('oldPassword', 'newPassword');

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/users/me/password', {
        currentPassword: 'oldPassword',
        newPassword: 'newPassword',
      });
    });
  });
});
