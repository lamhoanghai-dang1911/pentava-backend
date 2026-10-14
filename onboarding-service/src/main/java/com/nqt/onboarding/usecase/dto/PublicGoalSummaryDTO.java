package com.nqt.onboarding.usecase.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicGoalSummaryDTO {
    private Long goalId;
    private String goalName;
    private String description;
    private String authorName;
    private LocalDateTime createdAt;
    private List<CustomWeekDTO> weeks;
    private List<CustomTaskItemDTO> badTasks;
    private List<CustomTaskItemDTO> neutralTasks;
    private List<CustomTaskItemDTO> goodTasks;
}
