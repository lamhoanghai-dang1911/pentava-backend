package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.ResendOtpRequestDTO;

public interface ResendOtpInputPort {
    void resendOtp(ResendOtpRequestDTO requestDTO);
}