package com.nqt.cinema.usecase.support;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class GoalWeekCalculator {

    public static final int WEEKS_PER_GOAL = 4;

    private GoalWeekCalculator() {
    }

    public static LocalDate weekStartOf(LocalDate goalStartDate, int weekNumber) {
        return goalStartDate.plusWeeks(weekNumber - 1L);
    }

    public static LocalDate weekEndOf(LocalDate goalStartDate, int weekNumber) {
        return weekStartOf(goalStartDate, weekNumber).plusDays(6);
    }

    public static int weekNumberOf(LocalDate goalStartDate, LocalDate date) {
        long daysBetween = ChronoUnit.DAYS.between(goalStartDate, date);
        int n = (int) (daysBetween / 7) + 1;
        return Math.min(Math.max(n, 1), WEEKS_PER_GOAL);
    }
}