package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.VerifyOtpRequestDTO;

public interface VerifyOtpInputPort {
    void verifyOtp(VerifyOtpRequestDTO requestDTO);
}