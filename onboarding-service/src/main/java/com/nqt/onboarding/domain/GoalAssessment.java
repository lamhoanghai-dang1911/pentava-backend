package com.nqt.onboarding.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GoalAssessment {
    private Long id;
    private Long goalId;
    private Long userId;
    private String userEmail;

    private Integer baseSleepScore;
    private Integer baseExerciseScore;
    private Integer baseStressScore;
    private Integer baseDisciplineScore;
    private Double baseOverallScore;

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

    private String goalQuestionText;
    private Integer goalQuestionAnswer;
    private String goalQuestionAnswerLabel;

    private Integer selfPerceptionPercent;

    private LocalDateTime createdAt;
}
