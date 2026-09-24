package com.nqt.cinema.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Một ảnh check-in lấy từ checkin-service. createdAt có thể null với dữ liệu cũ. */
public record CheckinPhoto(
        Long id,
        Long taskProgressId,
        LocalDate checkinDate,
        String imageUrl,
        LocalDateTime createdAt
) {
}