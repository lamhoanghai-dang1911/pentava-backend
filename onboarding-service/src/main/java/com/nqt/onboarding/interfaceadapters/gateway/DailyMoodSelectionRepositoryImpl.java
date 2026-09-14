package com.nqt.onboarding.interfaceadapters.gateway;

import com.nqt.onboarding.infrastructure.persistence.DailyMoodSelectionEntity;
import com.nqt.onboarding.usecase.boundary.DailyMoodSelectionRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyMoodSelectionRepositoryImpl implements DailyMoodSelectionRepositoryGateway {

    private final DailyMoodSelectionJpaRepository jpaRepository;

    @Override
    public DailyMoodSelectionEntity save(DailyMoodSelectionEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<DailyMoodSelectionEntity> findByUserGoalAndDate(Long userId, Long goalId, LocalDate date) {
        return jpaRepository.findByUserIdAndGoalIdAndSelectionDate(userId, goalId, date);
    }
}