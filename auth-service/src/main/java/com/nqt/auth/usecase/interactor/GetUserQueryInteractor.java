package com.nqt.auth.usecase.interactor;

import com.nqt.auth.domain.User;
import com.nqt.auth.usecase.boundary.GetUserQueryInputPort;
import com.nqt.auth.usecase.boundary.OnboardingProfileGateway;
import com.nqt.auth.usecase.dto.UserQueryResponseDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetUserQueryInteractor implements GetUserQueryInputPort {

    private final UserRepositoryGateway userRepositoryGateway;
    private final OnboardingProfileGateway onboardingProfileGateway;
    private final com.nqt.auth.infrastructure.storage.SupabaseAvatarSignedUrlService supabaseAvatarSignedUrlService;

    public GetUserQueryInteractor(UserRepositoryGateway userRepositoryGateway,
                                  OnboardingProfileGateway onboardingProfileGateway,
                                  com.nqt.auth.infrastructure.storage.SupabaseAvatarSignedUrlService supabaseAvatarSignedUrlService) {
        this.userRepositoryGateway = userRepositoryGateway;
        this.onboardingProfileGateway = onboardingProfileGateway;
        this.supabaseAvatarSignedUrlService = supabaseAvatarSignedUrlService;
    }

    @Override
    public Optional<UserQueryResponseDTO> getUserByEmail(String email) {
        return userRepositoryGateway.findByEmail(email).map(user -> toDTO(user, resolveName(user)));
    }

    @Override
    public Optional<UserQueryResponseDTO> getUserById(Long id) {
        return userRepositoryGateway.findById(id).map(user -> toDTO(user, resolveName(user)));
    }

    private String resolveName(User user) {
        return onboardingProfileGateway.getProfileNameByEmail(user.getEmail())
                .or(() -> onboardingProfileGateway.getProfileNameByUserId(user.getId()))
                .filter(name -> !name.isBlank())
                .orElse(user.getName());
    }

    private UserQueryResponseDTO toDTO(User user, String resolvedName) {
        String signedAvatarUrl = supabaseAvatarSignedUrlService.createSignedUrl(user.getAvatarUrl());
        return UserQueryResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(resolvedName)
                .avatarUrl(signedAvatarUrl)
                .bio(user.getBio())
                .build();
    }
}