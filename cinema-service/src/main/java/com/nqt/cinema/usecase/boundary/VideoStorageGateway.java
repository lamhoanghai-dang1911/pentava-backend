package com.nqt.cinema.usecase.boundary;

import java.io.File;

public interface VideoStorageGateway {
    String upload(File videoFile, Long userId, Long clipId) throws Exception;
    String createSignedUrl(String storagePath, long expirySeconds) throws Exception;
    void delete(String storagePath) throws Exception;
}