package com.nqt.auth.interfaceadapters.presenter;

import com.nqt.auth.usecase.boundary.LoginOutputPort;
import com.nqt.auth.usecase.dto.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthPresenter implements LoginOutputPort {

    private ResponseEntity<?> responseEntity;

    public ResponseEntity<?> getResponseEntity() {
        return responseEntity;
    }

    @Override
    public void present(LoginResponseDTO responseDTO) {
        responseEntity = ResponseEntity.ok(Map.of(
                "success", true,
                "message", responseDTO.getMessage(),
                "data", responseDTO
        ));
    }

    @Override
    public void presentError(String errorMessage) {
        responseEntity = ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", errorMessage
        ));
    }
}