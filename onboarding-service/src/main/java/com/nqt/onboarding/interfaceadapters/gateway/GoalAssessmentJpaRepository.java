package com.nqt.onboarding.interfaceadapters.gateway;

import com.nqt.onboarding.infrastructure.persistence.GoalAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoalAssessmentJpaRepository extends JpaRepository<GoalAssessmentEntity, Long> {
    Optional<GoalAssessmentEntity> findByGoalId(Long goalId);
    Optional<GoalAssessmentEntity> findByGoalIdAndUserId(Long goalId, Long userId);
}
