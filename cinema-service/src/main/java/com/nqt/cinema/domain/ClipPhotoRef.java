package com.nqt.cinema.domain;

import java.time.LocalDate;

public record ClipPhotoRef(
        Long photoId,
        String imageUrl,
        LocalDate checkinDate,
        int displayOrder
) {
}