package com.platform.sdk.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client wrapper for SDK API calls.
 */
public class HttpClient {
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private String accessToken;

    public HttpClient(String baseUrl) {
        this(baseUrl, Duration.ofSeconds(30));
    }

    public HttpClient(String baseUrl, Duration timeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public <T> T get(String path, Class<T> responseType) throws ApiException {
        return get(path, responseType, null);
    }

    public <T> T get(String path, Class<T> responseType, Map<String, String> queryParams) throws ApiException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if (queryParams != null) {
            queryParams.forEach(urlBuilder::addQueryParameter);
        }

        Request request = buildRequest(urlBuilder.build())
                .get()
                .build();

        return execute(request, responseType);
    }

    public <T> T get(String path, TypeReference<T> typeReference) throws ApiException {
        return get(path, typeReference, null);
    }

    public <T> T get(String path, TypeReference<T> typeReference, Map<String, String> queryParams) throws ApiException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if (queryParams != null) {
            queryParams.forEach(urlBuilder::addQueryParameter);
        }

        Request request = buildRequest(urlBuilder.build())
                .get()
                .build();

        return execute(request, typeReference);
    }

    public <T> T post(String path, Object body, Class<T> responseType) throws ApiException {
        Request request = buildRequest(HttpUrl.parse(baseUrl + path))
                .post(createJsonBody(body))
                .build();

        return execute(request, responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) throws ApiException {
        Request request = buildRequest(HttpUrl.parse(baseUrl + path))
                .put(createJsonBody(body))
                .build();

        return execute(request, responseType);
    }

    public <T> T patch(String path, Object body, Class<T> responseType) throws ApiException {
        Request request = buildRequest(HttpUrl.parse(baseUrl + path))
                .patch(createJsonBody(body))
                .build();

        return execute(request, responseType);
    }

    public void delete(String path) throws ApiException {
        Request request = buildRequest(HttpUrl.parse(baseUrl + path))
                .delete()
                .build();

        executeVoid(request);
    }

    public void postVoid(String path, Object body) throws ApiException {
        Request request = buildRequest(HttpUrl.parse(baseUrl + path))
                .post(createJsonBody(body))
                .build();

        executeVoid(request);
    }

    private Request.Builder buildRequest(HttpUrl url) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }

        return builder;
    }

    private RequestBody createJsonBody(Object body) throws ApiException {
        try {
            String json = body == null ? "{}" : objectMapper.writeValueAsString(body);
            return RequestBody.create(json, MediaType.parse("application/json"));
        } catch (IOException e) {
            throw new ApiException("Failed to serialize request body", e);
        }
    }

    private <T> T execute(Request request, Class<T> responseType) throws ApiException {
        try (Response response = client.newCall(request).execute()) {
            handleErrorResponse(response);
            String body = response.body() != null ? response.body().string() : "";
            if (body.isEmpty() || responseType == Void.class) {
                return null;
            }
            return objectMapper.readValue(body, responseType);
        } catch (IOException e) {
            throw new ApiException("Request failed: " + e.getMessage(), e);
        }
    }

    private <T> T execute(Request request, TypeReference<T> typeReference) throws ApiException {
        try (Response response = client.newCall(request).execute()) {
            handleErrorResponse(response);
            String body = response.body() != null ? response.body().string() : "";
            if (body.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(body, typeReference);
        } catch (IOException e) {
            throw new ApiException("Request failed: " + e.getMessage(), e);
        }
    }

    private void executeVoid(Request request) throws ApiException {
        try (Response response = client.newCall(request).execute()) {
            handleErrorResponse(response);
        } catch (IOException e) {
            throw new ApiException("Request failed: " + e.getMessage(), e);
        }
    }

    private void handleErrorResponse(Response response) throws ApiException {
        if (!response.isSuccessful()) {
            String errorBody = "";
            try {
                if (response.body() != null) {
                    errorBody = response.body().string();
                }
            } catch (IOException ignored) {
            }

            switch (response.code()) {
                case 401:
                    throw new ApiException("Unauthorized: " + errorBody, 401, "UNAUTHORIZED");
                case 403:
                    throw new ApiException("Forbidden: " + errorBody, 403, "FORBIDDEN");
                case 404:
                    throw new ApiException("Not found: " + errorBody, 404, "NOT_FOUND");
                case 422:
                    throw new ApiException("Validation error: " + errorBody, 422, "VALIDATION_ERROR");
                default:
                    throw new ApiException("API error: " + errorBody, response.code());
            }
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
