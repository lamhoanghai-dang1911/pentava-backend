package com.nqt.onboarding.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoodSelectionRequestDTO {
    @NotNull(message = "goalId không được để trống")
    private Long goalId;

    @NotBlank(message = "moodType không được để trống")
    private String moodType; // "BAD" | "NEUTRAL" | "GOOD"

    private String comment;
}