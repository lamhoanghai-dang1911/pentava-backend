package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.RegisterRequestDTO;

public interface RegisterInputPort {
    void register(RegisterRequestDTO requestDTO);
}