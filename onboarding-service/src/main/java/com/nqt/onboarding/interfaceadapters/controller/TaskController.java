package com.nqt.onboarding.interfaceadapters.controller;

import com.nqt.onboarding.interfaceadapters.presenter.OnboardingPresenter;
import com.nqt.onboarding.usecase.boundary.OnboardingInputPort;
import com.nqt.onboarding.usecase.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class TaskController {

    private final OnboardingInputPort inputPort;
    private final OnboardingPresenter presenter;

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasksByWeek(@RequestParam("week") Integer week) {
        inputPort.getTasksByWeek(week);
        return presenter.getResponseEntity();
    }

    // Auto-save các item user đã tích chọn (Lưu nháp) - theo progressId (user_task_daily.id)
    @PostMapping("/tasks/daily/{progressId}/progress/items")
    public ResponseEntity<?> saveProgressItems(
            @PathVariable("progressId") Long progressId,
            @RequestBody TaskProgressRequestDTO requestDTO) {
        inputPort.saveTaskProgressItems(progressId, requestDTO);
        return presenter.getResponseEntity();
    }

    // Lấy trạng thái tiến độ đã chọn khi mở lại app - theo progressId
    @GetMapping("/tasks/daily/{progressId}/progress")
    public ResponseEntity<?> getProgress(@PathVariable("progressId") Long progressId) {
        inputPort.getTaskProgress(progressId);
        return presenter.getResponseEntity();
    }

    // Checkin - theo progressId
    @PostMapping("/tasks/daily/{progressId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable("progressId") Long progressId) {
        inputPort.completeTaskProgress(progressId);
        return presenter.getResponseEntity();
    }

    @GetMapping("/tasks/daily-status")
    public ResponseEntity<?> getDailyTaskStatus(@RequestParam("goalId") Long goalId) {
        inputPort.getDailyTaskStatus(goalId);
        return presenter.getResponseEntity();
    }

    @PostMapping("/mood/select")
    public ResponseEntity<?> selectMoodAndGetTasks(@RequestBody MoodSelectionRequestDTO requestDTO) {
        inputPort.selectMoodAndGetTasks(requestDTO);
        return presenter.getResponseEntity();
    }

    @PostMapping("/tasks/confirm-daily")
    public ResponseEntity<?> confirmDailyTasks(@RequestBody ConfirmDailyTasksRequestDTO requestDTO) {
        inputPort.confirmDailyTasks(requestDTO);
        return presenter.getResponseEntity();
    }

    @GetMapping("/goals/current")
    public ResponseEntity<?> getCurrentGoal() {
        inputPort.getCurrentGoal();
        return presenter.getResponseEntity();
    }

    @PostMapping("/tasks/swap")
    public ResponseEntity<?> swapDailyTask(@RequestBody SwapTaskRequestDTO requestDTO) {
        inputPort.swapDailyTask(requestDTO);
        return presenter.getResponseEntity();
    }

    @GetMapping("/streak/current")
    public ResponseEntity<?> getCurrentStreak() {
        inputPort.getCurrentStreak();
        return presenter.getResponseEntity();
    }

    @GetMapping("/tasks/history")
    public ResponseEntity<?> getTaskHistory(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        inputPort.getTaskHistory(startDate, endDate);
        return presenter.getResponseEntity();
    }

    // Danh mục các task đã làm trong quá khứ theo từng mục tiêu & tuần
    @GetMapping("/tasks/past-catalog")
    public ResponseEntity<?> getPastTasksCatalog() {
        inputPort.getPastTasksCatalog();
        return presenter.getResponseEntity();
    }

    // Tự thiết kế lộ trình (Custom Roadmap)
    @PostMapping("/goals/custom")
    public ResponseEntity<?> createCustomGoal(@Valid @RequestBody CreateCustomGoalRequestDTO requestDTO) {
        inputPort.createCustomGoal(requestDTO);
        return presenter.getResponseEntity();
    }

    // Bật/tắt chế độ công khai cho lộ trình tự thiết kế
    @PutMapping("/goals/{goalId}/visibility")
    public ResponseEntity<?> updateGoalVisibility(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody UpdateGoalVisibilityRequestDTO requestDTO) {
        inputPort.updateGoalVisibility(goalId, requestDTO);
        return presenter.getResponseEntity();
    }

    // Xem các lộ trình cộng đồng được chia sẻ công khai
    @GetMapping("/goals/public")
    public ResponseEntity<?> getPublicGoals() {
        inputPort.getPublicGoals();
        return presenter.getResponseEntity();
    }

    // Sao chép một lộ trình công khai làm lộ trình của mình
    @PostMapping("/goals/public/{publicGoalId}/clone")
    public ResponseEntity<?> clonePublicGoal(@PathVariable("publicGoalId") Long publicGoalId) {
        inputPort.clonePublicGoal(publicGoalId);
        return presenter.getResponseEntity();
    }
}