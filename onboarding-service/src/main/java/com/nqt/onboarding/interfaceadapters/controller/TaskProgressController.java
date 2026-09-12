package com.nqt.onboarding.interfaceadapters.controller;

import com.nqt.onboarding.interfaceadapters.presenter.TaskProgressPresenter;
import com.nqt.onboarding.usecase.boundary.TaskProgressInputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskProgressController {

    private final TaskProgressInputPort inputPort;
    private final TaskProgressPresenter presenter;

    // 1. API GET: Lấy tiến độ hiện tại của task trong ngày
    @GetMapping("/{taskId}/progress")
    public ResponseEntity<?> getTaskProgress(@PathVariable("taskId") Long taskId) {
        inputPort.getTaskProgress(taskId);
        return presenter.getResponseEntity();
    }

    // 2. API POST: Tăng tiến độ (mỗi lần bấm nút dấu cộng +)
    @PostMapping("/{taskId}/progress")
    public ResponseEntity<?> incrementProgress(@PathVariable("taskId") Long taskId) {
        inputPort.incrementTaskProgress(taskId);
        return presenter.getResponseEntity();
    }
}