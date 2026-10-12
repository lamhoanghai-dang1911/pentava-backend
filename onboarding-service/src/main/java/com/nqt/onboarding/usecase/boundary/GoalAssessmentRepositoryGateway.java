package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.domain.GoalAssessment;
import java.util.Optional;

public interface GoalAssessmentRepositoryGateway {
    Optional<GoalAssessment> findByGoalId(Long goalId);
    Optional<GoalAssessment> findByGoalIdAndUserId(Long goalId, Long userId);
    GoalAssessment save(GoalAssessment assessment);
}
