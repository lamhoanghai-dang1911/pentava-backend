package com.nqt.onboarding.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingResponseDTO {
    private String message;
    private DiagnosticReportDTO diagnostic;
    private List<TaskResponseDTO> tasks;
    private TaskProgressResponseDTO taskProgress;
    private MoodTaskResponseDTO moodTaskResponse;
    private Long goalId;
    private DailyTaskStatusResponseDTO dailyTaskStatus;
    private CurrentGoalResponseDTO currentGoal;
    private StreakResponseDTO streak;
    private List<DailyTaskHistoryDTO> taskHistory;
    private List<PastGoalCatalogDTO> pastTaskCatalog;
    private List<PublicGoalSummaryDTO> publicGoals;
    private GoalAssessmentContextDTO assessmentContext;
    private GoalAssessmentReportDTO assessmentReport;
}