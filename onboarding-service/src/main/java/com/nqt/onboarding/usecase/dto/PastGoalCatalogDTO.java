package com.nqt.onboarding.usecase.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastGoalCatalogDTO {
    private Long goalId;
    private String goalName;
    private Boolean isCustom;
    private List<PastTaskCatalogItemDTO> tasks;
}
