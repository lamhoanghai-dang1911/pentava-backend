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

    @PostMapping("/task-progress/{taskProgressId}")
    public ResponseEntity<?> saveCheckinImage(
            @PathVariable("taskProgressId") Long taskProgressId,
            @RequestBody CheckinRequestDTO requestDTO) {
        inputPort.saveCheckinImageUrl(taskProgressId, requestDTO);
        return presenter.getResponseEntity();
    }

    @GetMapping("/task-progress/{taskProgressId}")
    public ResponseEntity<?> getCheckinImage(
            @PathVariable("taskProgressId") Long taskProgressId) {
        inputPort.getCheckinByTask(taskProgressId);
        return presenter.getResponseEntity();
    }
}