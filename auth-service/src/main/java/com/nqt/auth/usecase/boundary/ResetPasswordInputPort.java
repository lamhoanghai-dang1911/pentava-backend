package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.ResetPasswordRequestDTO;

public interface ResetPasswordInputPort {
    void resetPassword(ResetPasswordRequestDTO requestDTO);
}