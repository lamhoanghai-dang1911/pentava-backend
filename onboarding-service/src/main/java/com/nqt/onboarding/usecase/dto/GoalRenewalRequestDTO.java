package com.nqt.onboarding.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class GoalRenewalRequestDTO {
    @NotBlank(message = "Mục tiêu không được để trống")
    private String primaryGoal;

    @NotBlank(message = "Loại routine không được để trống")
    private String routineType;

    @NotBlank(message = "Giờ ngủ không được để trống")
    private String sleepHours;

    @NotBlank(message = "Tần suất tập luyện không được để trống")
    private String exerciseFrequency;

    @NotBlank(message = "Tần suất stress không được để trống")
    private String stressFrequency;

    @NotBlank(message = "Thời gian màn hình không được để trống")
    private String screenTime;

    @NotBlank(message = "Thói quen duy trì không được để trống")
    private String habitDuration;

    @NotEmpty(message = "Phải chọn ít nhất một khoảng thời gian rảnh")
    private List<String> freeTimes;
}