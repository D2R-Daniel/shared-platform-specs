package com.platform.sdk.notifications;

import com.fasterxml.jackson.core.type.TypeReference;
import com.platform.sdk.common.ApiException;
import com.platform.sdk.common.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client for notification operations.
 */
public class NotificationClient {
    private final HttpClient httpClient;

    private NotificationClient(Builder builder) {
        this.httpClient = new HttpClient(builder.baseUrl, builder.timeout);
        if (builder.accessToken != null) {
            this.httpClient.setAccessToken(builder.accessToken);
        }
    }

    /**
     * Set the access token for authenticated requests.
     */
    public void setAccessToken(String accessToken) {
        this.httpClient.setAccessToken(accessToken);
    }

    /**
     * List notifications with optional filtering and pagination.
     */
    public NotificationListResponse list(ListNotificationsParams params) throws ApiException {
        Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
        return httpClient.get("/api/notifications", NotificationListResponse.class, queryParams);
    }

    /**
     * List notifications with default parameters.
     */
    public NotificationListResponse list() throws ApiException {
        return list(null);
    }

    /**
     * Get a notification by ID.
     */
    public Notification get(String notificationId) throws ApiException {
        return httpClient.get("/api/notifications/" + notificationId, Notification.class);
    }

    /**
     * Mark a notification as read.
     */
    public Notification markAsRead(String notificationId) throws ApiException {
        return httpClient.patch("/api/notifications/" + notificationId + "/read", null, Notification.class);
    }

    /**
     * Mark all notifications as read.
     */
    public void markAllAsRead() throws ApiException {
        httpClient.postVoid("/api/notifications/read-all", null);
    }

    /**
     * Delete a notification.
     */
    public void delete(String notificationId) throws ApiException {
        httpClient.delete("/api/notifications/" + notificationId);
    }

    /**
     * Get unread notification count.
     */
    public UnreadCountResponse getUnreadCount() throws ApiException {
        return httpClient.get("/api/notifications/unread-count", UnreadCountResponse.class);
    }

    /**
     * Get notification preferences.
     */
    public NotificationPreferences getPreferences() throws ApiException {
        return httpClient.get("/api/notifications/preferences", NotificationPreferences.class);
    }

    /**
     * Update notification preferences.
     */
    public NotificationPreferences updatePreferences(UpdatePreferencesRequest request) throws ApiException {
        return httpClient.patch("/api/notifications/preferences", request, NotificationPreferences.class);
    }

    /**
     * Get registered devices.
     */
    public List<RegisteredDevice> getDevices() throws ApiException {
        return httpClient.get("/api/notifications/devices", new TypeReference<List<RegisteredDevice>>() {});
    }

    /**
     * Register a device for push notifications.
     */
    public RegisteredDevice registerDevice(String token, String platform, String name, String model) throws ApiException {
        Map<String, String> body = new java.util.HashMap<>();
        body.put("token", token);
        body.put("platform", platform);
        if (name != null) body.put("name", name);
        if (model != null) body.put("model", model);
        return httpClient.post("/api/notifications/devices", body, RegisteredDevice.class);
    }

    /**
     * Unregister a device.
     */
    public void unregisterDevice(String deviceId) throws ApiException {
        httpClient.delete("/api/notifications/devices/" + deviceId);
    }

    /**
     * Builder for NotificationClient.
     */
    public static class Builder {
        private String baseUrl;
        private String accessToken;
        private Duration timeout = Duration.ofSeconds(30);

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public NotificationClient build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            return new NotificationClient(this);
        }
    }

    /**
     * Unread count response.
     */
    public static class UnreadCountResponse {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    /**
     * Request to update notification preferences.
     */
    public static class UpdatePreferencesRequest {
        private Boolean emailEnabled;
        private Boolean smsEnabled;
        private Boolean pushEnabled;
        private Boolean inAppEnabled;
        private String digestFrequency;

        public UpdatePreferencesRequest emailEnabled(Boolean emailEnabled) {
            this.emailEnabled = emailEnabled;
            return this;
        }

        public UpdatePreferencesRequest smsEnabled(Boolean smsEnabled) {
            this.smsEnabled = smsEnabled;
            return this;
        }

        public UpdatePreferencesRequest pushEnabled(Boolean pushEnabled) {
            this.pushEnabled = pushEnabled;
            return this;
        }

        public UpdatePreferencesRequest inAppEnabled(Boolean inAppEnabled) {
            this.inAppEnabled = inAppEnabled;
            return this;
        }

        public UpdatePreferencesRequest digestFrequency(String digestFrequency) {
            this.digestFrequency = digestFrequency;
            return this;
        }

        public Boolean getEmailEnabled() {
            return emailEnabled;
        }

        public Boolean getSmsEnabled() {
            return smsEnabled;
        }

        public Boolean getPushEnabled() {
            return pushEnabled;
        }

        public Boolean getInAppEnabled() {
            return inAppEnabled;
        }

        public String getDigestFrequency() {
            return digestFrequency;
        }
    }
}
