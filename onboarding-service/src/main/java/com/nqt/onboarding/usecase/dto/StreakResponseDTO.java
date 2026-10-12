package com.nqt.onboarding.usecase.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StreakResponseDTO {
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastCompletedDate;
    private Integer freezesUsedThisMonth;
    private Integer maxFreezesPerMonth;
    private Boolean hasStreakProtection;
    private Boolean streakFrozenYesterday;
}