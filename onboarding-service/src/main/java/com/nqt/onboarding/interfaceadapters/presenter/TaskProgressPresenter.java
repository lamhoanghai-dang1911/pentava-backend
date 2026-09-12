package com.nqt.onboarding.interfaceadapters.presenter;

import com.nqt.onboarding.usecase.boundary.TaskProgressOutputPort;
import com.nqt.onboarding.usecase.dto.TaskProgressResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskProgressPresenter implements TaskProgressOutputPort {

    private ResponseEntity<?> responseEntity;

    @Override
    public void present(TaskProgressResponseDTO response) {
        this.responseEntity = ResponseEntity.ok(response);
    }

    @Override
    public void presentError(String message) {
        this.responseEntity = ResponseEntity.badRequest().body(message);
    }

    public ResponseEntity<?> getResponseEntity() {
        return responseEntity;
    }
}