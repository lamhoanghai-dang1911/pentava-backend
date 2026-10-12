package com.nqt.onboarding.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalAssessmentReportDTO {
    private Long goalId;
    private String goalName;
    private LocalDateTime completedAt;

    // Baseline scores
    private Integer baseSleepScore;
    private Integer baseExerciseScore;
    private Integer baseStressScore;
    private Integer baseDisciplineScore;
    private Double baseOverallScore;

    // Post scores
    private String postSleepAnswer;
    private Integer postSleepScore;

    private String postExerciseAnswer;
    private Integer postExerciseScore;

    private String postStressAnswer;
    private Integer postStressScore;

    private String postDisciplineAnswer;
    private Integer postDisciplineScore;

    private Double postOverallScore;
    private Double deltaScore;

    // Dynamic goal answer
    private String goalQuestionText;
    private Integer goalQuestionAnswer;
    private String goalQuestionAnswerLabel;

    // Self perception
    private Integer selfPerceptionPercent;
}
