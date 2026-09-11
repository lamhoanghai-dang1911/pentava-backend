package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.LoginResponseDTO;

public interface LoginOutputPort {
    void present(LoginResponseDTO responseDTO);
    void presentError(String errorMessage);
}
