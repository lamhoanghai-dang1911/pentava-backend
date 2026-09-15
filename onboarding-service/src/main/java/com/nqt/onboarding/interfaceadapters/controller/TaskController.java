package com.nqt.onboarding.interfaceadapters.controller;

import com.nqt.onboarding.interfaceadapters.presenter.OnboardingPresenter;
import com.nqt.onboarding.usecase.boundary.OnboardingInputPort;
import com.nqt.onboarding.usecase.dto.ConfirmDailyTasksRequestDTO;
import com.nqt.onboarding.usecase.dto.MoodSelectionRequestDTO;
import com.nqt.onboarding.usecase.dto.SwapTaskRequestDTO;
import com.nqt.onboarding.usecase.dto.TaskProgressRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ➕ API 1: Auto-save các item user đã tích chọn (Lưu nháp)
    @PostMapping("/tasks/{taskId}/progress/items")
    public ResponseEntity<?> saveProgressItems(
            @PathVariable("taskId") Long taskId,
            @RequestBody TaskProgressRequestDTO requestDTO) {
        inputPort.saveTaskProgressItems(taskId, requestDTO);
        return presenter.getResponseEntity();
    }

    // ➕ API 2: Lấy trạng thái tiến độ đã chọn khi mở lại app
    @GetMapping("/tasks/{taskId}/progress")
    public ResponseEntity<?> getProgress(@PathVariable("taskId") Long taskId) {
        inputPort.getTaskProgress(taskId);
        return presenter.getResponseEntity();
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable("taskId") Long taskId) {
        inputPort.completeTaskProgress(taskId);
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
}