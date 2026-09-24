package com.nqt.cinema.usecase.dto;

import java.time.LocalDateTime;

public record ClipDTO(
        Long clipId,
        Long goalId,
        int weekNumber,
        String status,
        String videoUrl,
        String errorMessage,
        int photoCount,
        LocalDateTime createdAt
) {
}