package com.nqt.onboarding.usecase.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyTaskHistoryDTO {
    private LocalDate taskDate;
    private List<TaskResponseDTO> tasks;
}