package com.nqt.onboarding.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoalProgressionRequestDTO {
    @NotBlank(message = "Tên mục tiêu hiện tại không được để trống")
    private String currentGoalName;
    @NotBlank(message = "Tên mục tiêu tiếp theo không được để trống")
    private String nextGoalName;
}