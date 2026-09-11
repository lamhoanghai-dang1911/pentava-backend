package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.UserQueryResponseDTO;

import java.util.Optional;

public interface GetUserQueryInputPort {
    Optional<UserQueryResponseDTO> getUserByEmail(String email);

    Optional<UserQueryResponseDTO> getUserById(Long id);
}