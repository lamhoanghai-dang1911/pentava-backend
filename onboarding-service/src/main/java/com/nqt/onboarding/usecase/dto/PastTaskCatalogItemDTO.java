package com.nqt.onboarding.usecase.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastTaskCatalogItemDTO {
    private Long taskId;
    private Integer weekNumber;
    private String title;
    private String content;
    private String moodType;
    private Boolean isCompleted;
}
