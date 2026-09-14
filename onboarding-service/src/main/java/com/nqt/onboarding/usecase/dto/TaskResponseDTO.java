package com.nqt.onboarding.usecase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String content;
    private Integer weekNumber;
    private Boolean isAiGenerated;
    private Boolean isCompleted;
    private String moodType; // ➕ mới, để FE biết task thuộc mood nào
}