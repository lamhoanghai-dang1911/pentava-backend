package com.nqt.onboarding.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingAnswerDTO {
    private String userEmail;
    private String name;
    private String age;
    private String gender;
    private String primaryGoal;           // ➕ Thay thế goals list
    private String routineType;           // ➕ Mới
    private String sleepHours;
    private String exerciseFrequency;
    private String stressFrequency;
    private String screenTime;
    private String habitDuration;
    private List<String> freeTimes;
}