package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.LoginRequestDTO;

public interface LoginInputPort {
    void login(LoginRequestDTO requestDTO);
}