package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.ForgotPasswordRequestDTO;

public interface ForgotPasswordInputPort {
    void forgotPassword(ForgotPasswordRequestDTO requestDTO);
}