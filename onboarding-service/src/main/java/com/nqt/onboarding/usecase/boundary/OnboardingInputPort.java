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

    // ➕ Lộ trình tự thiết kế & công khai cộng đồng
    void getPastTasksCatalog();
    void createCustomGoal(CreateCustomGoalRequestDTO requestDTO);
    void updateGoalVisibility(Long goalId, UpdateGoalVisibilityRequestDTO requestDTO);
    void getPublicGoals();
    void clonePublicGoal(Long publicGoalId);
    void clonePublicGoal(Long publicGoalId, String applyMode);

    // ➕ Khảo sát đầu ra & Báo cáo chuyển hóa
    void getGoalAssessmentContext(Long goalId);
    void submitGoalAssessment(Long goalId, GoalAssessmentRequestDTO requestDTO);
    void getGoalAssessmentReport(Long goalId);
}