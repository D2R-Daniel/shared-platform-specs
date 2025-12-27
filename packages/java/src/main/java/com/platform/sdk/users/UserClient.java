package com.platform.sdk.users;

import com.platform.sdk.common.ApiException;
import com.platform.sdk.common.HttpClient;

import java.time.Duration;
import java.util.Map;

/**
 * Client for user management operations.
 */
public class UserClient {
    private final HttpClient httpClient;

    private UserClient(Builder builder) {
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
     * List users with optional filtering and pagination.
     */
    public UserListResponse list(ListUsersParams params) throws ApiException {
        Map<String, String> queryParams = params != null ? params.toQueryParams() : Map.of();
        return httpClient.get("/api/users", UserListResponse.class, queryParams);
    }

    /**
     * List users with default parameters.
     */
    public UserListResponse list() throws ApiException {
        return list(null);
    }

    /**
     * Get a user by ID.
     */
    public User get(String userId) throws ApiException {
        return httpClient.get("/api/users/" + userId, User.class);
    }

    /**
     * Create a new user.
     */
    public User create(CreateUserRequest request) throws ApiException {
        return httpClient.post("/api/users", request, User.class);
    }

    /**
     * Update an existing user.
     */
    public User update(String userId, UpdateUserRequest request) throws ApiException {
        return httpClient.patch("/api/users/" + userId, request, User.class);
    }

    /**
     * Delete a user.
     */
    public void delete(String userId) throws ApiException {
        httpClient.delete("/api/users/" + userId);
    }

    /**
     * Update user status.
     */
    public User updateStatus(String userId, String status, String reason) throws ApiException {
        Map<String, String> body = Map.of("status", status, "reason", reason != null ? reason : "");
        return httpClient.patch("/api/users/" + userId + "/status", body, User.class);
    }

    /**
     * Get the current user's profile.
     */
    public UserProfile getMyProfile() throws ApiException {
        return httpClient.get("/api/users/me", UserProfile.class);
    }

    /**
     * Update the current user's profile.
     */
    public UserProfile updateMyProfile(UpdateProfileRequest request) throws ApiException {
        return httpClient.patch("/api/users/me", request, UserProfile.class);
    }

    /**
     * Get the current user's preferences.
     */
    public UserPreferences getMyPreferences() throws ApiException {
        return httpClient.get("/api/users/me/preferences", UserPreferences.class);
    }

    /**
     * Update the current user's preferences.
     */
    public UserPreferences updateMyPreferences(UserPreferences preferences) throws ApiException {
        return httpClient.patch("/api/users/me/preferences", preferences, UserPreferences.class);
    }

    /**
     * Change the current user's password.
     */
    public void changePassword(String currentPassword, String newPassword) throws ApiException {
        Map<String, String> body = Map.of(
                "current_password", currentPassword,
                "new_password", newPassword
        );
        httpClient.postVoid("/api/users/me/password", body);
    }

    /**
     * Builder for UserClient.
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

        public UserClient build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            return new UserClient(this);
        }
    }

    /**
     * Request to update user profile.
     */
    public static class UpdateProfileRequest {
        private String name;
        private String bio;
        private String phone;
        private String timezone;
        private String locale;

        public UpdateProfileRequest name(String name) {
            this.name = name;
            return this;
        }

        public UpdateProfileRequest bio(String bio) {
            this.bio = bio;
            return this;
        }

        public UpdateProfileRequest phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UpdateProfileRequest timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public UpdateProfileRequest locale(String locale) {
            this.locale = locale;
            return this;
        }

        public String getName() {
            return name;
        }

        public String getBio() {
            return bio;
        }

        public String getPhone() {
            return phone;
        }

        public String getTimezone() {
            return timezone;
        }

        public String getLocale() {
            return locale;
        }
    }
}
