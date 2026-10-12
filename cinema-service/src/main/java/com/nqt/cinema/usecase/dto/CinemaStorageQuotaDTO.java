package com.nqt.cinema.usecase.dto;

public record CinemaStorageQuotaDTO(
        long usedBytes,
        long totalBytes,
        long usedVideosBytes,
        long usedPhotosBytes,
        long totalClipsCount,
        long totalPhotosCount,
        String planCode,
        String planName,
        boolean isSubscriptionActive,
        double usagePercentage,
        String formattedUsed,
        String formattedTotal
) {
}
