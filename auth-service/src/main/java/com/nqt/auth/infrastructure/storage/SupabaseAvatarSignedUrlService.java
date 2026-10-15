package com.nqt.auth.infrastructure.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SupabaseAvatarSignedUrlService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAvatarSignedUrlService.class);

    private final String baseUrl;
    private final String serviceKey;
    private final String bucket;
    private final long expirySeconds;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    // Cache bộ nhớ đệm: storagePath -> CachedSignedUrl
    private final Map<String, CachedSignedUrl> cache = new ConcurrentHashMap<>();

    private record CachedSignedUrl(String url, Instant expiresAt) {}

    public SupabaseAvatarSignedUrlService(
            @Value("${supabase.url:https://olgtvgfgpaydkopjhern.supabase.co}") String baseUrl,
            @Value("${supabase.service-key:}") String serviceKey,
            @Value("${supabase.avatar-bucket:avatar}") String bucket,
            @Value("${supabase.signed-url-expiry-seconds:21600}") long expirySeconds,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        this.serviceKey = serviceKey == null ? "" : serviceKey.trim();
        this.bucket = bucket;
        this.expirySeconds = expirySeconds;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Tạo hoặc lấy từ cache đường dẫn có chữ ký (Signed URL) cho file avatar trong Supabase private bucket.
     */
    public String createSignedUrl(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            return pathOrUrl;
        }

        // Đã là signed URL hợp lệ
        if (pathOrUrl.contains("/storage/v1/object/sign/") && pathOrUrl.contains("token=")) {
            return pathOrUrl;
        }

        // Không có serviceKey hoặc baseUrl thì giữ nguyên
        if (serviceKey.isBlank() || baseUrl.isBlank()) {
            return pathOrUrl;
        }

        String storagePath = extractStoragePath(pathOrUrl);
        if (storagePath == null || storagePath.isBlank()) {
            return pathOrUrl;
        }

        // Kiểm tra cache (dành 5 phút trừ hao trước khi hết hạn)
        CachedSignedUrl cached = cache.get(storagePath);
        Instant now = Instant.now();
        if (cached != null && cached.expiresAt().isAfter(now.plus(Duration.ofMinutes(5)))) {
            return cached.url();
        }

        try {
            String signEndpoint = baseUrl + "/storage/v1/object/sign/" + bucket + "/" + storagePath;
            String requestBody = "{\"expiresIn\":" + expirySeconds + "}";

            HttpRequest request = HttpRequest.newBuilder(URI.create(signEndpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.warn("Tạo Supabase signed URL cho avatar thất bại (HTTP {}): {}", response.statusCode(), response.body());
                return pathOrUrl;
            }

            JsonNode node = objectMapper.readTree(response.body());
            if (!node.has("signedURL")) {
                return pathOrUrl;
            }

            String signedPath = node.get("signedURL").asText();
            String fullSignedUrl = baseUrl + "/storage/v1" + signedPath;

            cache.put(storagePath, new CachedSignedUrl(fullSignedUrl, now.plus(Duration.ofSeconds(expirySeconds))));
            return fullSignedUrl;
        } catch (Exception e) {
            log.error("Lỗi khi tạo signed URL cho avatar {}: {}", storagePath, e.getMessage());
            return pathOrUrl;
        }
    }

    /**
     * Trích xuất đường dẫn tương đối (users/123/xxx.jpg) từ path hoặc URL
     */
    public String extractStoragePath(String pathOrUrl) {
        if (pathOrUrl == null) return null;
        String clean = pathOrUrl.trim();

        // 1. Dạng users/... trực tiếp
        if (clean.startsWith("users/")) {
            return clean.split("\\?")[0];
        }

        // 2. Chứa /users/ trong URL
        int usersIdx = clean.indexOf("/users/");
        if (usersIdx >= 0) {
            String path = clean.substring(usersIdx + 1);
            return path.split("\\?")[0];
        }

        // 3. Nếu là file name nằm ngay trong bucket avatar
        int bucketIdx = clean.indexOf("/" + bucket + "/");
        if (bucketIdx >= 0) {
            String path = clean.substring(bucketIdx + bucket.length() + 2);
            return path.split("\\?")[0];
        }

        return null;
    }
}
