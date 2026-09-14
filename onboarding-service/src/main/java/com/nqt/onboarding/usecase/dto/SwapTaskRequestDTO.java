package com.nqt.onboarding.usecase.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SwapTaskRequestDTO {
    private Long goalId;
    private Long oldTaskId;
    private Long newTaskId;
}