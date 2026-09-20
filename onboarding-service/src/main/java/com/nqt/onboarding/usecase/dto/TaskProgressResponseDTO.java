package com.nqt.onboarding.usecase.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TaskProgressResponseDTO {
    private String message;
    private Long progressId; // ➕ id dòng trong user_task_daily
    private Long taskId;
    private Integer currentCount;
    private Integer targetCount;
    private Boolean isCompleted;
    private LocalDate taskDate;

    private List<String> selectedItems;
}