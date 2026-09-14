package com.nqt.onboarding.usecase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ConfirmDailyTasksRequestDTO {
    @NotNull(message = "goalId không được để trống")
    private Long goalId;

    @NotEmpty(message = "Phải chọn nhiệm vụ")
    private List<Long> selectedTaskIds;
}