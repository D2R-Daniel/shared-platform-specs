import { describe, it, expect, vi, beforeEach } from 'vitest';
import { NotificationClient } from '../notifications';
import type { Notification, NotificationPreferences, RegisteredDevice } from '../notifications';

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

describe('NotificationClient', () => {
  let client: NotificationClient;

  const sampleNotification: Notification = {
    id: 'notif-123',
    type: 'in_app',
    category: 'account',
    title: 'Welcome',
    body: 'Welcome to the platform!',
    read: false,
    createdAt: '2024-01-01T00:00:00Z',
  };

  const samplePreferences: NotificationPreferences = {
    emailEnabled: true,
    pushEnabled: true,
    digestFrequency: 'daily',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    client = new NotificationClient({
      baseUrl: 'https://api.example.com',
      accessToken: 'test-token',
    });
  });

  describe('list', () => {
    it('should list notifications', async () => {
      const mockResponse = {
        data: {
          data: [sampleNotification],
          pagination: {
            page: 1,
            pageSize: 20,
            totalItems: 1,
            totalPages: 1,
          },
        },
      };

      mockGet.mockResolvedValueOnce(mockResponse);

      const result = await client.list();

      expect(result.data).toHaveLength(1);
      expect(result.data[0].id).toBe('notif-123');
    });

    it('should list unread notifications only', async () => {
      const mockResponse = {
        data: {
          data: [sampleNotification],
          pagination: {
            page: 1,
            pageSize: 20,
            totalItems: 1,
            totalPages: 1,
          },
        },
      };

      mockGet.mockResolvedValueOnce(mockResponse);

      await client.list({ status: 'unread' });

      expect(mockGet).toHaveBeenCalledWith('/notifications', {
        params: expect.objectContaining({ status: 'unread' }),
      });
    });
  });

  describe('get', () => {
    it('should get a single notification', async () => {
      mockGet.mockResolvedValueOnce({ data: sampleNotification });

      const notification = await client.get('notif-123');

      expect(notification.id).toBe('notif-123');
      expect(notification.title).toBe('Welcome');
      expect(mockGet).toHaveBeenCalledWith('/notifications/notif-123');
    });
  });

  describe('markAsRead', () => {
    it('should mark notification as read', async () => {
      const readNotification = { ...sampleNotification, read: true };
      mockPost.mockResolvedValueOnce({ data: readNotification });

      const notification = await client.markAsRead('notif-123');

      expect(notification.read).toBe(true);
      expect(mockPost).toHaveBeenCalledWith('/notifications/notif-123/read');
    });
  });

  describe('markAllAsRead', () => {
    it('should mark all notifications as read', async () => {
      mockPost.mockResolvedValueOnce({ data: { updated_count: 5 } });

      const count = await client.markAllAsRead();

      expect(count).toBe(5);
      expect(mockPost).toHaveBeenCalledWith('/notifications/read-all', {
        category: undefined,
        before: undefined,
      });
    });

    it('should mark all notifications in category as read', async () => {
      mockPost.mockResolvedValueOnce({ data: { updated_count: 3 } });

      const count = await client.markAllAsRead('account');

      expect(count).toBe(3);
      expect(mockPost).toHaveBeenCalledWith('/notifications/read-all', {
        category: 'account',
        before: undefined,
      });
    });
  });

  describe('delete', () => {
    it('should delete a notification', async () => {
      mockDelete.mockResolvedValueOnce({ data: {} });

      await client.delete('notif-123');

      expect(mockDelete).toHaveBeenCalledWith('/notifications/notif-123');
    });
  });

  describe('getUnreadCount', () => {
    it('should get unread count', async () => {
      mockGet.mockResolvedValueOnce({ data: { count: 5 } });

      const result = await client.getUnreadCount();

      expect(result.count).toBe(5);
      expect(mockGet).toHaveBeenCalledWith('/notifications/unread-count');
    });

    it('should get unread count by category', async () => {
      mockGet.mockResolvedValueOnce({
        data: {
          count: 10,
          by_category: { account: 3, alerts: 7 },
        },
      });

      const result = await client.getUnreadCount();

      expect(result.count).toBe(10);
      expect(result.byCategory).toEqual({ account: 3, alerts: 7 });
    });
  });

  describe('preferences', () => {
    it('should get preferences', async () => {
      mockGet.mockResolvedValueOnce({ data: samplePreferences });

      const prefs = await client.getPreferences();

      expect(prefs.emailEnabled).toBe(true);
      expect(prefs.digestFrequency).toBe('daily');
      expect(mockGet).toHaveBeenCalledWith('/notifications/preferences');
    });

    it('should update preferences', async () => {
      const updatedPrefs = { ...samplePreferences, digestFrequency: 'weekly' };
      mockPut.mockResolvedValueOnce({ data: updatedPrefs });

      const prefs = await client.updatePreferences({ digestFrequency: 'weekly' });

      expect(prefs.digestFrequency).toBe('weekly');
      expect(mockPut).toHaveBeenCalledWith('/notifications/preferences', { digestFrequency: 'weekly' });
    });
  });

  describe('categories', () => {
    it('should list categories', async () => {
      mockGet.mockResolvedValueOnce({
        data: {
          categories: [{ id: 'account', name: 'Account', description: 'Account notifications' }],
        },
      });

      const categories = await client.listCategories();

      expect(categories).toHaveLength(1);
      expect(categories[0].id).toBe('account');
      expect(mockGet).toHaveBeenCalledWith('/notifications/categories');
    });
  });

  describe('subscriptions', () => {
    it('should list subscriptions', async () => {
      mockGet.mockResolvedValueOnce({
        data: {
          subscriptions: [
            { id: 'sub-123', channel: 'email', topic: 'alerts', subscribedAt: '2024-01-01T00:00:00Z' },
          ],
        },
      });

      const subscriptions = await client.listSubscriptions();

      expect(subscriptions).toHaveLength(1);
      expect(subscriptions[0].channel).toBe('email');
      expect(mockGet).toHaveBeenCalledWith('/notifications/subscriptions');
    });

    it('should subscribe to a channel', async () => {
      mockPost.mockResolvedValueOnce({
        data: { id: 'sub-123', channel: 'push', topic: 'updates', subscribedAt: '2024-01-01T00:00:00Z' },
      });

      const subscription = await client.subscribe('push', 'updates');

      expect(subscription.channel).toBe('push');
      expect(mockPost).toHaveBeenCalledWith('/notifications/subscriptions', {
        channel: 'push',
        topic: 'updates',
        endpoint: undefined,
      });
    });

    it('should unsubscribe from a channel', async () => {
      mockDelete.mockResolvedValueOnce({ data: {} });

      await client.unsubscribe('sub-123');

      expect(mockDelete).toHaveBeenCalledWith('/notifications/subscriptions/sub-123');
    });
  });

  describe('devices', () => {
    const sampleDevice: RegisteredDevice = {
      id: 'device-123',
      platform: 'android',
      name: 'My Phone',
      registeredAt: '2024-01-01T00:00:00Z',
    };

    it('should list devices', async () => {
      mockGet.mockResolvedValueOnce({
        data: { devices: [sampleDevice] },
      });

      const devices = await client.listDevices();

      expect(devices).toHaveLength(1);
      expect(devices[0].platform).toBe('android');
      expect(mockGet).toHaveBeenCalledWith('/notifications/devices');
    });

    it('should register a device', async () => {
      mockPost.mockResolvedValueOnce({ data: sampleDevice });

      const device = await client.registerDevice('fcm-token', 'android', 'My Phone');

      expect(device.id).toBe('device-123');
      expect(device.platform).toBe('android');
      expect(mockPost).toHaveBeenCalledWith('/notifications/devices', {
        token: 'fcm-token',
        platform: 'android',
        name: 'My Phone',
        model: undefined,
      });
    });

    it('should unregister a device', async () => {
      mockDelete.mockResolvedValueOnce({ data: {} });

      await client.unregisterDevice('device-123');

      expect(mockDelete).toHaveBeenCalledWith('/notifications/devices/device-123');
    });
  });

  describe('sendTest', () => {
    it('should send a test notification', async () => {
      mockPost.mockResolvedValueOnce({
        data: { message: 'Test notification sent', notificationId: 'test-123' },
      });

      const result = await client.sendTest('push', 'Hello!');

      expect(result.message).toBe('Test notification sent');
      expect(mockPost).toHaveBeenCalledWith('/notifications/test', {
        channel: 'push',
        message: 'Hello!',
      });
    });
  });
});
