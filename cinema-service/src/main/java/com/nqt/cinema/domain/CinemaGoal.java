package com.nqt.cinema.domain;

import java.time.LocalDate;

public record CinemaGoal(
        Long id,
        String goalName,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        boolean expired
) {
}