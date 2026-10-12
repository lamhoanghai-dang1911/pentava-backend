package com.nqt.onboarding.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalAssessmentContextDTO {
    private Long goalId;
    private String goalName;
    private Boolean isCustom;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer daysElapsed;
    private Boolean isCompleted;
    private Boolean alreadyAssessed;

    // Baseline info from profile
    private String baseSleepHours;
    private String baseExerciseFrequency;
    private String baseStressFrequency;
    private String baseHabitDuration;
    private String baseRoutine;

    // Calculated baseline scores
    private Integer baseSleepScore;
    private Integer baseExerciseScore;
    private Integer baseStressScore;
    private Integer baseDisciplineScore;
    private Double baseOverallScore;
}
