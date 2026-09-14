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
public class DiagnosticReportDTO {
    private double overallScore;
    private String healthStatus;
    private List<String> topIssues;
    private List<String> recommendedGoals;
    private String summary;
    private MoodTaskPlanDTO moodTaskPlan; // thay thế actionPlans

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodTaskPlanDTO {
        private String goal;
        private String description;
        private List<String> badTasks;     // đúng 20
        private List<String> neutralTasks; // đúng 20
        private List<String> goodTasks;    // đúng 20
    }
}