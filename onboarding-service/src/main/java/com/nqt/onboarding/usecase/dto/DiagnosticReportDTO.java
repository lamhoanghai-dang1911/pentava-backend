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
    private double overallScore;              // 1-10
    private String healthStatus;              // "excellent / good / fair / poor / critical"
    private List<String> topIssues;           // ["High stress", "Poor sleep", ...]
    private List<String> recommendedGoals;    // ["Reduce stress", "Improve sleep", ...]
    private String summary;                   // Chi tiết phân tích
    private List<ActionPlanDTO> actionPlans;  // Plan cho mỗi goal

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionPlanDTO {
        private String goal;
        private String description;
        private List<String> actions;         // ["Week 1-2: ...", "Week 3-4: ...", ...]
    }
}