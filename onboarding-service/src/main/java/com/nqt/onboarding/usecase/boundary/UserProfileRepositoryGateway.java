package com.nqt.onboarding.usecase.boundary;
import com.nqt.onboarding.domain.UserProfile;
import java.util.Optional;

public interface UserProfileRepositoryGateway {
    Optional<UserProfile> findByEmail(String email);
    UserProfile save(UserProfile profile);
}