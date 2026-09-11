package com.nqt.checkin.interfaceadapters.presenter;

import com.nqt.checkin.usecase.boundary.CheckinOutputPort;
import com.nqt.checkin.usecase.dto.CheckinResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CheckinPresenter implements CheckinOutputPort {

    private ResponseEntity<?> responseEntity;

    @Override
    public void present(CheckinResponseDTO response) {
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