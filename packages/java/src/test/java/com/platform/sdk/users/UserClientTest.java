package com.platform.sdk.users;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserClient Tests")
class UserClientTest {

    private MockWebServer mockWebServer;
    private UserClient userClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        userClient = new UserClient.Builder()
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
                new UserClient.Builder().build()
        );
    }

    @Test
    @DisplayName("get should fetch user by ID")
    void getUserById() throws Exception {
        String responseBody = """
                {
                    "id": "user-123",
                    "email": "test@example.com",
                    "name": "Test User",
                    "status": "active",
                    "roles": ["user"]
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        User user = userClient.get("user-123");

        assertEquals("user-123", user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getName());
        assertEquals("active", user.getStatus());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/users/user-123", request.getPath());
        assertTrue(request.getHeader("Authorization").contains("Bearer test-token"));
    }

    @Test
    @DisplayName("list should fetch users with pagination")
    void listUsers() throws Exception {
        String responseBody = """
                {
                    "data": [
                        {
                            "id": "user-123",
                            "email": "test@example.com",
                            "name": "Test User",
                            "status": "active"
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

        UserListResponse response = userClient.list();

        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("user-123", response.getData().get(0).getId());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().startsWith("/api/users"));
    }

    @Test
    @DisplayName("create should post new user")
    void createUser() throws Exception {
        String responseBody = """
                {
                    "id": "new-user-123",
                    "email": "new@example.com",
                    "name": "New User",
                    "status": "pending",
                    "roles": ["user"]
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("new@example.com");
        request.setName("New User");

        User user = userClient.create(request);

        assertEquals("new-user-123", user.getId());
        assertEquals("new@example.com", user.getEmail());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/users", recordedRequest.getPath());
    }

    @Test
    @DisplayName("delete should send delete request")
    void deleteUser() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        userClient.delete("user-123");

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/api/users/user-123", request.getPath());
    }

    @Test
    @DisplayName("getMyProfile should fetch current user profile")
    void getMyProfile() throws Exception {
        String responseBody = """
                {
                    "id": "user-123",
                    "email": "test@example.com",
                    "name": "Test User",
                    "bio": "A test user"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        UserProfile profile = userClient.getMyProfile();

        assertEquals("user-123", profile.getId());
        assertEquals("test@example.com", profile.getEmail());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/api/users/me", request.getPath());
    }

    @Test
    @DisplayName("changePassword should post password change")
    void changePassword() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        userClient.changePassword("oldPassword", "newPassword");

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/users/me/password", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("current_password"));
        assertTrue(body.contains("new_password"));
    }

    @Test
    @DisplayName("updateStatus should patch user status")
    void updateStatus() throws Exception {
        String responseBody = """
                {
                    "id": "user-123",
                    "email": "test@example.com",
                    "status": "suspended"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        User user = userClient.updateStatus("user-123", "suspended", "Policy violation");

        assertEquals("suspended", user.getStatus());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertEquals("/api/users/user-123/status", request.getPath());
    }
}
