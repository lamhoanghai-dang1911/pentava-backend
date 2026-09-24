package com.nqt.cinema.usecase.dto;

import java.time.LocalDate;

public record GoalSummaryDTO(
        Long goalId,
        String goalName,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        boolean expired
) {
}