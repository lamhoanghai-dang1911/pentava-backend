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

    @NotEmpty(message = "Lộ trình phải có ít nhất 1 tuần có nhiệm vụ")
    @Valid
    private List<CustomWeekDTO> weeks;
}
