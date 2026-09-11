package com.nqt.checkin.usecase.boundary;

import com.nqt.checkin.usecase.dto.CheckinResponseDTO;

public interface CheckinOutputPort {
    void present(CheckinResponseDTO response);
    void presentError(String message);
}