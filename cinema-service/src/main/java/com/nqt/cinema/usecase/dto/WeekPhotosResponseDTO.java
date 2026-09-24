package com.nqt.cinema.usecase.dto;

import java.time.LocalDate;
import java.util.List;

public record WeekPhotosResponseDTO(
        Long goalId,
        int weekNumber,
        LocalDate weekStart,
        LocalDate weekEnd,
        List<PhotoDTO> photos
) {
}