package com.nqt.onboarding.interfaceadapters.controller;

import com.nqt.onboarding.interfaceadapters.presenter.OnboardingPresenter;
import com.nqt.onboarding.usecase.boundary.OnboardingInputPort;
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
    public ResponseEntity<?> completeTask(
            @PathVariable("taskId") Long taskId,
            @RequestBody(required = false) TaskProgressRequestDTO requestDTO) {
        inputPort.completeTaskProgress(taskId, requestDTO);
        return presenter.getResponseEntity();
    }
}