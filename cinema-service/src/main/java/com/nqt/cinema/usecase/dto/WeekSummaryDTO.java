package com.nqt.cinema.usecase.dto;

import java.time.LocalDate;

public record WeekSummaryDTO(
        Long goalId,
        int weekNumber,
        LocalDate weekStart,
        LocalDate weekEnd,
        int photoCount,
        boolean eligible,      // đã bắt đầu && đủ ảnh
        int photosNeeded,
        boolean current        // hôm nay nằm trong tuần này
) {
}