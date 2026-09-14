package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.infrastructure.persistence.DailyMoodSelectionEntity;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyMoodSelectionRepositoryGateway {
    DailyMoodSelectionEntity save(DailyMoodSelectionEntity entity);
    Optional<DailyMoodSelectionEntity> findByUserGoalAndDate(Long userId, Long goalId, LocalDate date);
}