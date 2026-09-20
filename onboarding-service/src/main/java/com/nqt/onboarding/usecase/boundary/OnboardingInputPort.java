package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.usecase.dto.*;

import java.time.LocalDate;

public interface OnboardingInputPort {
    void submitOnboarding(OnboardingRequestDTO requestDTO);
    void progressToNextGoal(GoalProgressionRequestDTO requestDTO);
    void getTasksByWeek(Integer weekNumber);

    // ➕ 3 method dưới đây thao tác theo id của bảng user_task_daily (progressId), không còn theo taskId
    void saveTaskProgressItems(Long progressId, TaskProgressRequestDTO requestDTO);
    void getTaskProgress(Long progressId);
    void completeTaskProgress(Long progressId);

    void selectMoodAndGetTasks(MoodSelectionRequestDTO requestDTO);
    void confirmDailyTasks(ConfirmDailyTasksRequestDTO requestDTO);
    void submitGoalRenewal(GoalRenewalRequestDTO requestDTO);
    void getDailyTaskStatus(Long goalId);
    void getCurrentGoal();
    void swapDailyTask(SwapTaskRequestDTO requestDTO);
    void getCurrentStreak();
    void getTaskHistory(LocalDate startDate, LocalDate endDate);
}