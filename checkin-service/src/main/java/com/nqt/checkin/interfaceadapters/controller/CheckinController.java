package com.nqt.checkin.interfaceadapters.controller;

import com.nqt.checkin.interfaceadapters.presenter.CheckinPresenter;
import com.nqt.checkin.usecase.boundary.CheckinInputPort;
import com.nqt.checkin.usecase.dto.CheckinRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinInputPort inputPort;
    private final CheckinPresenter presenter;

    // 1. API POST: Lưu URL ảnh check-in từ Supabase do Frontend gửi lên
    @PostMapping("/tasks/{taskId}")
    public ResponseEntity<?> saveCheckinImage(
            @PathVariable("taskId") Long taskId,
            @RequestBody CheckinRequestDTO requestDTO) {
        inputPort.saveCheckinImageUrl(taskId, requestDTO);
        return presenter.getResponseEntity();
    }

    // 2. API GET: Lấy thông tin ảnh check-in của task trong ngày hiện tại
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getCheckinImage(
            @PathVariable("taskId") Long taskId) {
        inputPort.getCheckinByTask(taskId);
        return presenter.getResponseEntity();
    }
}