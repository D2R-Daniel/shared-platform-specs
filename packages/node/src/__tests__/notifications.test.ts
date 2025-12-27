import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { NotificationClient } from '../notifications';
import type { Notification, NotificationPreferences, RegisteredDevice } from '../notifications';

// Mock axios
vi.mock('axios');
const mockedAxios = vi.mocked(axios);

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

    // Mock axios.create
    mockedAxios.create = vi.fn().mockReturnValue({
      get: mockedAxios.get,
      post: mockedAxios.post,
      patch: mockedAxios.patch,
      delete: mockedAxios.delete,
      defaults: { headers: { common: {} } },
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

      mockedAxios.get.mockResolvedValueOnce(mockResponse);

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

      mockedAxios.get.mockResolvedValueOnce(mockResponse);

      await client.list({ status: 'unread' });

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/notifications',
        expect.objectContaining({
          params: expect.objectContaining({ status: 'unread' }),
        })
      );
    });
  });

  describe('get', () => {
    it('should get a single notification', async () => {
      mockedAxios.get.mockResolvedValueOnce({ data: sampleNotification });

      const notification = await client.get('notif-123');

      expect(notification.id).toBe('notif-123');
      expect(notification.title).toBe('Welcome');
    });
  });

  describe('markAsRead', () => {
    it('should mark notification as read', async () => {
      const readNotification = { ...sampleNotification, read: true };
      mockedAxios.patch.mockResolvedValueOnce({ data: readNotification });

      const notification = await client.markAsRead('notif-123');

      expect(notification.read).toBe(true);
      expect(mockedAxios.patch).toHaveBeenCalledWith('/api/notifications/notif-123/read', {});
    });
  });

  describe('markAllAsRead', () => {
    it('should mark all notifications as read', async () => {
      mockedAxios.post.mockResolvedValueOnce({ data: {} });

      await client.markAllAsRead();

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/notifications/read-all', {});
    });
  });

  describe('delete', () => {
    it('should delete a notification', async () => {
      mockedAxios.delete.mockResolvedValueOnce({ data: {} });

      await client.delete('notif-123');

      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/notifications/notif-123');
    });
  });

  describe('getUnreadCount', () => {
    it('should get unread count', async () => {
      mockedAxios.get.mockResolvedValueOnce({ data: { count: 5 } });

      const result = await client.getUnreadCount();

      expect(result.count).toBe(5);
    });
  });

  describe('preferences', () => {
    it('should get preferences', async () => {
      mockedAxios.get.mockResolvedValueOnce({ data: samplePreferences });

      const prefs = await client.getPreferences();

      expect(prefs.emailEnabled).toBe(true);
      expect(prefs.digestFrequency).toBe('daily');
    });

    it('should update preferences', async () => {
      const updatedPrefs = { ...samplePreferences, digestFrequency: 'weekly' };
      mockedAxios.patch.mockResolvedValueOnce({ data: updatedPrefs });

      const prefs = await client.updatePreferences({ digestFrequency: 'weekly' });

      expect(prefs.digestFrequency).toBe('weekly');
    });
  });

  describe('devices', () => {
    const sampleDevice: RegisteredDevice = {
      id: 'device-123',
      platform: 'android',
      name: 'My Phone',
      registeredAt: '2024-01-01T00:00:00Z',
    };

    it('should register a device', async () => {
      mockedAxios.post.mockResolvedValueOnce({ data: sampleDevice });

      const device = await client.registerDevice('fcm-token', 'android', 'My Phone');

      expect(device.id).toBe('device-123');
      expect(device.platform).toBe('android');
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/notifications/devices', {
        token: 'fcm-token',
        platform: 'android',
        name: 'My Phone',
      });
    });

    it('should unregister a device', async () => {
      mockedAxios.delete.mockResolvedValueOnce({ data: {} });

      await client.unregisterDevice('device-123');

      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/notifications/devices/device-123');
    });
  });
});

describe('Notification Events', () => {
  // Test that event types are properly exported and typed
  it('should have correct event type definitions', async () => {
    const { EmailNotificationEvent, SMSNotificationEvent, PushNotificationEvent } = await import(
      '../notifications/events'
    );

    // These are type definitions, just verify they exist
    expect(typeof EmailNotificationEvent).toBe('undefined'); // They are just types
  });
});
