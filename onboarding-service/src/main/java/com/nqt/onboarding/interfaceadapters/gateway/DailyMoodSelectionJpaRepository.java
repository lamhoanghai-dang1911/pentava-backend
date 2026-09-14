package com.nqt.onboarding.interfaceadapters.gateway;

import com.nqt.onboarding.infrastructure.persistence.DailyMoodSelectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyMoodSelectionJpaRepository extends JpaRepository<DailyMoodSelectionEntity, Long> {
    Optional<DailyMoodSelectionEntity> findByUserIdAndGoalIdAndSelectionDate(Long userId, Long goalId, LocalDate selectionDate);
}