package com.platform.sdk.notifications;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationClient Tests")
class NotificationClientTest {

    private MockWebServer mockWebServer;
    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        notificationClient = new NotificationClient.Builder()
                .baseUrl(baseUrl)
                .accessToken("test-token")
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Builder should require baseUrl")
    void builderRequiresBaseUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                new NotificationClient.Builder().build()
        );
    }

    @Test
    @DisplayName("get should fetch notification by ID")
    void getNotificationById() throws Exception {
        String responseBody = """
                {
                    "id": "notif-123",
                    "type": "in_app",
                    "category": "account",
                    "title": "Welcome",
                    "body": "Welcome to the platform!",
                    "read": false
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        Notification notification = notificationClient.get("notif-123");

        assertEquals("notif-123", notification.getId());
        assertEquals("Welcome", notification.getTitle());
        assertFalse(notification.isRead());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/notifications/notif-123", request.getPath());
    }

    @Test
    @DisplayName("list should fetch notifications with pagination")
    void listNotifications() throws Exception {
        String responseBody = """
                {
                    "data": [
                        {
                            "id": "notif-123",
                            "type": "in_app",
                            "title": "Welcome",
                            "read": false
                        }
                    ],
                    "pagination": {
                        "page": 1,
                        "pageSize": 20,
                        "totalItems": 1,
                        "totalPages": 1
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        NotificationListResponse response = notificationClient.list();

        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("notif-123", response.getData().get(0).getId());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().startsWith("/api/notifications"));
    }

    @Test
    @DisplayName("markAsRead should patch notification")
    void markAsRead() throws Exception {
        String responseBody = """
                {
                    "id": "notif-123",
                    "type": "in_app",
                    "title": "Welcome",
                    "read": true
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        Notification notification = notificationClient.markAsRead("notif-123");

        assertTrue(notification.isRead());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertEquals("/api/notifications/notif-123/read", request.getPath());
    }

    @Test
    @DisplayName("markAllAsRead should post to read-all endpoint")
    void markAllAsRead() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        notificationClient.markAllAsRead();

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/notifications/read-all", request.getPath());
    }

    @Test
    @DisplayName("delete should send delete request")
    void deleteNotification() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        notificationClient.delete("notif-123");

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/api/notifications/notif-123", request.getPath());
    }

    @Test
    @DisplayName("getUnreadCount should fetch count")
    void getUnreadCount() throws Exception {
        String responseBody = """
                {
                    "count": 5
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        NotificationClient.UnreadCountResponse response = notificationClient.getUnreadCount();

        assertEquals(5, response.getCount());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/notifications/unread-count", request.getPath());
    }

    @Test
    @DisplayName("getPreferences should fetch preferences")
    void getPreferences() throws Exception {
        String responseBody = """
                {
                    "email_enabled": true,
                    "push_enabled": true,
                    "digest_frequency": "daily"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        NotificationPreferences prefs = notificationClient.getPreferences();

        assertTrue(prefs.getEmailEnabled());
        assertTrue(prefs.getPushEnabled());
        assertEquals("daily", prefs.getDigestFrequency());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/notifications/preferences", request.getPath());
    }

    @Test
    @DisplayName("updatePreferences should patch preferences")
    void updatePreferences() throws Exception {
        String responseBody = """
                {
                    "email_enabled": false,
                    "push_enabled": true,
                    "digest_frequency": "weekly"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        NotificationClient.UpdatePreferencesRequest updateRequest =
                new NotificationClient.UpdatePreferencesRequest()
                        .emailEnabled(false)
                        .digestFrequency("weekly");

        NotificationPreferences prefs = notificationClient.updatePreferences(updateRequest);

        assertFalse(prefs.getEmailEnabled());
        assertEquals("weekly", prefs.getDigestFrequency());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertEquals("/api/notifications/preferences", request.getPath());
    }

    @Test
    @DisplayName("registerDevice should post device registration")
    void registerDevice() throws Exception {
        String responseBody = """
                {
                    "id": "device-123",
                    "platform": "android",
                    "name": "My Phone"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        RegisteredDevice device = notificationClient.registerDevice(
                "fcm-token", "android", "My Phone", null);

        assertEquals("device-123", device.getId());
        assertEquals("android", device.getPlatform());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/notifications/devices", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("fcm-token"));
        assertTrue(body.contains("android"));
    }

    @Test
    @DisplayName("unregisterDevice should delete device")
    void unregisterDevice() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        notificationClient.unregisterDevice("device-123");

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/api/notifications/devices/device-123", request.getPath());
    }
}
