package com.nqt.cinema.infrastructure.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nqt.cinema.usecase.boundary.VideoStorageGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

@Component
public class SupabaseVideoStorageAdapter implements VideoStorageGateway {

    private final String baseUrl;
    private final String serviceKey;
    private final String bucket;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public SupabaseVideoStorageAdapter(@Value("${supabase.url}") String baseUrl,
                                       @Value("${supabase.service-key}") String serviceKey,
                                       @Value("${supabase.cinema-bucket:cinema-clips}") String bucket,
                                       ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.objectMapper = objectMapper;
    }

    @Override
    public String upload(File videoFile, Long userId, Long clipId) throws Exception {
        String path = "users/" + userId + "/clip-" + clipId + ".mp4";
        byte[] bytes = Files.readAllBytes(videoFile.toPath());

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(baseUrl + "/storage/v1/object/" + bucket + "/" + path))
                .header("Authorization", "Bearer " + serviceKey)
                .header("apikey", serviceKey)
                .header("Content-Type", "video/mp4")
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Upload Supabase thất bại (HTTP " + response.statusCode() + "): " + response.body());
        }
        return path;
    }

    @Override
    public String createSignedUrl(String storagePath, long expirySeconds) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(baseUrl + "/storage/v1/object/sign/" + bucket + "/" + storagePath))
                .header("Authorization", "Bearer " + serviceKey)
                .header("apikey", serviceKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"expiresIn\":" + expirySeconds + "}"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Tạo signed URL thất bại (HTTP " + response.statusCode() + "): " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());
        String signedPath = node.get("signedURL").asText();
        return baseUrl + "/storage/v1" + signedPath;
    }

    @Override
    public void delete(String storagePath) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(baseUrl + "/storage/v1/object/" + bucket + "/" + storagePath))
                .header("Authorization", "Bearer " + serviceKey)
                .header("apikey", serviceKey)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Xoá file Supabase thất bại (HTTP " + response.statusCode() + "): " + response.body());
        }
    }
}