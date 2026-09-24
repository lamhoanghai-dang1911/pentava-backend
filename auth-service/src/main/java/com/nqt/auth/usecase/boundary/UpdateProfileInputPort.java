package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.UpdateProfileRequestDTO;
import com.nqt.auth.usecase.dto.UserQueryResponseDTO;

public interface UpdateProfileInputPort {
    UserQueryResponseDTO updateProfile(String email, UpdateProfileRequestDTO requestDTO);
}
