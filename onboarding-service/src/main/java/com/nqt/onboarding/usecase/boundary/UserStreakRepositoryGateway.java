package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.infrastructure.persistence.UserStreakEntity;
import java.util.Optional;

public interface UserStreakRepositoryGateway {
    Optional<UserStreakEntity> findByUserId(Long userId);
    UserStreakEntity save(UserStreakEntity entity);
}