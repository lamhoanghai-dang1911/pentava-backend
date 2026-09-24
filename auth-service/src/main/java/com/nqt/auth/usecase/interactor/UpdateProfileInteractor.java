package com.nqt.auth.usecase.interactor;

import com.nqt.auth.domain.User;
import com.nqt.auth.usecase.boundary.OnboardingProfileGateway;
import com.nqt.auth.usecase.boundary.UpdateProfileInputPort;
import com.nqt.auth.usecase.dto.UpdateProfileRequestDTO;
import com.nqt.auth.usecase.dto.UserQueryResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class UpdateProfileInteractor implements UpdateProfileInputPort {

    private final UserRepositoryGateway userRepositoryGateway;
    private final OnboardingProfileGateway onboardingProfileGateway;

    public UpdateProfileInteractor(UserRepositoryGateway userRepositoryGateway,
                                  OnboardingProfileGateway onboardingProfileGateway) {
        this.userRepositoryGateway = userRepositoryGateway;
        this.onboardingProfileGateway = onboardingProfileGateway;
    }

    @Override
    public UserQueryResponseDTO updateProfile(String email, UpdateProfileRequestDTO requestDTO) {
        User user = userRepositoryGateway.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với email: " + email));

        if (requestDTO.getName() != null && !requestDTO.getName().isBlank()) {
            user.setName(requestDTO.getName());
            onboardingProfileGateway.updateProfileName(user.getId(), user.getEmail(), requestDTO.getName());
        }
        if (requestDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(requestDTO.getAvatarUrl());
        }
        if (requestDTO.getBio() != null) {
            user.setBio(requestDTO.getBio());
        }

        User saved = userRepositoryGateway.save(user);
        return UserQueryResponseDTO.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .avatarUrl(saved.getAvatarUrl())
                .bio(saved.getBio())
                .build();
    }
}
