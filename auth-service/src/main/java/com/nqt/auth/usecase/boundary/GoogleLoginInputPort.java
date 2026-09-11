package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.GoogleLoginRequestDTO;

public interface GoogleLoginInputPort {
    void googleLogin(GoogleLoginRequestDTO requestDTO);
}