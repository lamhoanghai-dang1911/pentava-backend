package com.nqt.onboarding.usecase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGoalVisibilityRequestDTO {
    @NotNull(message = "Trạng thái công khai không được để trống")
    private Boolean isPublic;
}
