package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.domain.UserGoal;
import java.util.Optional;

public interface UserGoalRepositoryGateway {
    Optional<UserGoal> findByEmailAndGoalName(String email, String goalName);
    Optional<UserGoal> findById(Long id); // ➕ mới
    UserGoal save(UserGoal goal);
}