package com.nqt.onboarding.usecase.boundary;

import java.util.Optional;

public interface UserServiceGateway {
    Optional<Long> getUserIdByEmail(String email);

    Optional<String> getUserEmailById(Long userId);
    void updateOnboardingStatus(Long userId, boolean hasCompleted);
}