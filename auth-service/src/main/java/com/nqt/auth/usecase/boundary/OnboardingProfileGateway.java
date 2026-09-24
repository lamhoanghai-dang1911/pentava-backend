package com.nqt.auth.usecase.boundary;

import java.util.Optional;

public interface OnboardingProfileGateway {
    Optional<String> getProfileNameByEmail(String email);
    Optional<String> getProfileNameByUserId(Long userId);
    boolean updateProfileName(Long userId, String email, String name);
}
