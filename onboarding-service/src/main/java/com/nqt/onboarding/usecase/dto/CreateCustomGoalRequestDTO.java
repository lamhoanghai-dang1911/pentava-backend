package com.nqt.onboarding.usecase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCustomGoalRequestDTO {
    @NotBlank(message = "Tên mục tiêu không được để trống")
    private String goalName;

    private String description;

    @Builder.Default
    private Boolean isPublic = false;

    @Valid
    private List<CustomTaskItemDTO> badTasks;

    @Valid
    private List<CustomTaskItemDTO> neutralTasks;

    @Valid
    private List<CustomTaskItemDTO> goodTasks;

    @Valid
    private List<CustomWeekDTO> weeks;
}
