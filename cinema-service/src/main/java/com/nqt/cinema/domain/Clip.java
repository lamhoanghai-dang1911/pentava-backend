package com.nqt.cinema.domain;

import java.time.LocalDateTime;
import java.util.List;

public record Clip(
        Long id,
        Long userId,
        Long goalId,
        int weekNumber,
        ClipStatus status,
        String storagePath,
        Long fileSizeBytes,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ClipPhotoRef> photos
) {
}