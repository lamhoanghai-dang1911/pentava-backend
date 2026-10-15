package com.nqt.shop.infrastructure.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class SupabaseShopSignedUrlService {

    private final String baseUrl;
    private final String serviceKey;
    private final String skinBucket;
    private final String thumbnailBucket;
    private final long expirySeconds;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    // Cache bộ nhớ đệm: "bucket:path" -> CachedSignedUrl
    private final Map<String, CachedSignedUrl> cache = new ConcurrentHashMap<>();

    private record CachedSignedUrl(String url, Instant expiresAt) {}

    public SupabaseShopSignedUrlService(
            @Value("${supabase.url:https://olgtvgfgpaydkopjhern.supabase.co}") String baseUrl,
            @Value("${supabase.service-key:}") String serviceKey,
            @Value("${supabase.skin-bucket:skin}") String skinBucket,
            @Value("${supabase.thumbnail-bucket:shop_thumbnail}") String thumbnailBucket,
            @Value("${supabase.signed-url-expiry-seconds:21600}") long expirySeconds,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        this.serviceKey = serviceKey == null ? "" : serviceKey.trim();
        this.skinBucket = skinBucket;
        this.thumbnailBucket = thumbnailBucket;
        this.expirySeconds = expirySeconds;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String createSignedUrl(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            return pathOrUrl;
        }

        if (pathOrUrl.contains("/storage/v1/object/sign/") && pathOrUrl.contains("token=")) {
            return pathOrUrl;
        }

        if (serviceKey.isBlank() || baseUrl.isBlank()) {
            return pathOrUrl;
        }

        String targetBucket = resolveBucket(pathOrUrl);
        String storagePath = extractStoragePath(pathOrUrl, targetBucket);
        if (storagePath == null || storagePath.isBlank()) {
            return pathOrUrl;
        }

        String cacheKey = targetBucket + ":" + storagePath;
        CachedSignedUrl cached = cache.get(cacheKey);
        Instant now = Instant.now();
        if (cached != null && cached.expiresAt().isAfter(now.plus(Duration.ofMinutes(5)))) {
            return cached.url();
        }

        try {
            String signEndpoint = baseUrl + "/storage/v1/object/sign/" + targetBucket + "/" + storagePath;
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
                log.warn("Tạo signed URL shop thất bại (HTTP {}): {}", response.statusCode(), response.body());
                return pathOrUrl;
            }

            JsonNode node = objectMapper.readTree(response.body());
            if (!node.has("signedURL")) {
                return pathOrUrl;
            }

            String signedPath = node.get("signedURL").asText();
            String fullSignedUrl = baseUrl + "/storage/v1" + signedPath;

            cache.put(cacheKey, new CachedSignedUrl(fullSignedUrl, now.plus(Duration.ofSeconds(expirySeconds))));
            return fullSignedUrl;
        } catch (Exception e) {
            log.error("Lỗi khi tạo signed URL cho {}: {}", cacheKey, e.getMessage());
            return pathOrUrl;
        }
    }

    private String resolveBucket(String pathOrUrl) {
        if (pathOrUrl.contains("/" + skinBucket + "/") || pathOrUrl.endsWith(".png") && !pathOrUrl.contains("background")) {
            if (pathOrUrl.contains("/" + thumbnailBucket + "/")) {
                return thumbnailBucket;
            }
            return skinBucket;
        }
        return thumbnailBucket;
    }

    private String extractStoragePath(String pathOrUrl, String bucket) {
        String clean = pathOrUrl.trim().split("\\?")[0];
        int bucketIdx = clean.indexOf("/" + bucket + "/");
        if (bucketIdx >= 0) {
            return clean.substring(bucketIdx + bucket.length() + 2);
        }
        // Nếu chỉ là file name đơn thuần
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash >= 0) {
            return clean.substring(lastSlash + 1);
        }
        return clean;
    }
}
