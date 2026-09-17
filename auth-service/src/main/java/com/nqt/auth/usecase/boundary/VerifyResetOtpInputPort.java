package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.VerifyResetOtpRequestDTO;

public interface VerifyResetOtpInputPort {
    void verifyResetOtp(VerifyResetOtpRequestDTO requestDTO);
}