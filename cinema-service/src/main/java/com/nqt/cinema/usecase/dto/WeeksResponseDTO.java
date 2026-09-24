package com.nqt.cinema.usecase.dto;

import java.util.List;

public record WeeksResponseDTO(
        Long goalId,
        String goalName,
        int minPhotos,
        List<WeekSummaryDTO> weeks
) {
}