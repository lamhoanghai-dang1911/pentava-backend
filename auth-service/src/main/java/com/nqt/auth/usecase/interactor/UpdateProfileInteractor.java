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
    private final com.nqt.auth.infrastructure.storage.SupabaseAvatarSignedUrlService supabaseAvatarSignedUrlService;

    public UpdateProfileInteractor(UserRepositoryGateway userRepositoryGateway,
                                  OnboardingProfileGateway onboardingProfileGateway,
                                  com.nqt.auth.infrastructure.storage.SupabaseAvatarSignedUrlService supabaseAvatarSignedUrlService) {
        this.userRepositoryGateway = userRepositoryGateway;
        this.onboardingProfileGateway = onboardingProfileGateway;
        this.supabaseAvatarSignedUrlService = supabaseAvatarSignedUrlService;
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
            String extracted = supabaseAvatarSignedUrlService.extractStoragePath(requestDTO.getAvatarUrl());
            user.setAvatarUrl(extracted != null ? extracted : requestDTO.getAvatarUrl());
        }
        if (requestDTO.getBio() != null) {
            user.setBio(requestDTO.getBio());
        }

        User saved = userRepositoryGateway.save(user);
        String signedAvatarUrl = supabaseAvatarSignedUrlService.createSignedUrl(saved.getAvatarUrl());
        return UserQueryResponseDTO.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .avatarUrl(signedAvatarUrl)
                .bio(saved.getBio())
                .build();
    }
}
