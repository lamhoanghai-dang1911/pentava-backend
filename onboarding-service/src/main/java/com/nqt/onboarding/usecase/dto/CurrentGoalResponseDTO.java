package com.nqt.onboarding.usecase.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CurrentGoalResponseDTO {
    private Long goalId;
    private String goalName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean isCustom;
    private Boolean isPublic;
    private String description;
}