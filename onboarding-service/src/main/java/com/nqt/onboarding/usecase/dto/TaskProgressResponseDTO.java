package com.nqt.onboarding.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressResponseDTO {
    private Long taskId;
    private LocalDate taskDate;
    private Boolean isCompleted;
    private List<String> selectedItems;
}