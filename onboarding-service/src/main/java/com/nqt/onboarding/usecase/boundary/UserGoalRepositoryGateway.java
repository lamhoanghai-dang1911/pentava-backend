package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.domain.UserGoal;
import com.nqt.onboarding.domain.GoalStatus;
import java.util.Optional;

public interface UserGoalRepositoryGateway {
    Optional<UserGoal> findByEmailAndGoalName(String email, String goalName);
    Optional<UserGoal> findById(Long id);
    Optional<UserGoal> findByUserIdAndStatus(Long userId, GoalStatus status); // ➕ mới
    UserGoal save(UserGoal goal);
}