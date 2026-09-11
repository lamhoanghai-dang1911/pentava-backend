package com.nqt.onboarding.usecase.boundary;
//package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.usecase.dto.OnboardingRequestDTO;
import com.nqt.onboarding.usecase.dto.GoalProgressionRequestDTO;
import com.nqt.onboarding.usecase.dto.TaskProgressRequestDTO;
import com.nqt.onboarding.usecase.dto.TaskResponseDTO;

import java.util.List;

public interface OnboardingInputPort {
    void submitOnboarding(OnboardingRequestDTO requestDTO);
    void progressToNextGoal(GoalProgressionRequestDTO requestDTO);
    void getTasksByWeek(Integer weekNumber);
    void saveTaskProgressItems(Long taskId, TaskProgressRequestDTO requestDTO);
    void getTaskProgress(Long taskId);
    void completeTaskProgress(Long taskId, TaskProgressRequestDTO requestDTO);
}