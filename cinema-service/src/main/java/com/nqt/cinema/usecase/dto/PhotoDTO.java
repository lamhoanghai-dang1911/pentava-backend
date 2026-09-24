package com.nqt.cinema.usecase.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PhotoDTO(
        Long photoId,
        Long taskProgressId,
        LocalDate checkinDate,
        String imageUrl,
        LocalDateTime createdAt
) {
}