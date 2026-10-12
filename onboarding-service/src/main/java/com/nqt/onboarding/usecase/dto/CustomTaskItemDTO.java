package com.nqt.onboarding.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomTaskItemDTO {
    @NotBlank(message = "Tiêu đề nhiệm vụ không được để trống")
    private String title;

    private String content;

    private Long sourceTaskId;
}
