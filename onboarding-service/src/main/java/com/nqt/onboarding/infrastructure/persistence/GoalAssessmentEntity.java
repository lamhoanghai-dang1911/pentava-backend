package com.nqt.onboarding.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalAssessmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long goalId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userEmail;

    // Snapshot Baseline
    private Integer baseSleepScore;
    private Integer baseExerciseScore;
    private Integer baseStressScore;
    private Integer baseDisciplineScore;
    private Double baseOverallScore;

    // Post-Assessment 4 core metrics
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

    // Dynamic Goal Question
    private String goalQuestionText;
    private Integer goalQuestionAnswer;
    private String goalQuestionAnswerLabel;

    // Self-perception
    private Integer selfPerceptionPercent;

    private LocalDateTime createdAt;
}
