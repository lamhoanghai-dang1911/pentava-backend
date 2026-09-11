package com.nqt.auth.usecase.interactor;

import com.nqt.auth.domain.User;
import com.nqt.auth.usecase.boundary.GetUserQueryInputPort;
import com.nqt.auth.usecase.dto.UserQueryResponseDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetUserQueryInteractor implements GetUserQueryInputPort {

    private final UserRepositoryGateway userRepositoryGateway;

    public GetUserQueryInteractor(UserRepositoryGateway userRepositoryGateway) {
        this.userRepositoryGateway = userRepositoryGateway;
    }

    @Override
    public Optional<UserQueryResponseDTO> getUserByEmail(String email) {
        return userRepositoryGateway.findByEmail(email).map(this::toDTO);
    }

    @Override
    public Optional<UserQueryResponseDTO> getUserById(Long id) {
        return userRepositoryGateway.findById(id).map(this::toDTO);
    }

    private UserQueryResponseDTO toDTO(User user) {
        return UserQueryResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}