package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.infrastructure.persistence.TaskProgressEntity;
import java.time.LocalDate;
import java.util.Optional;

public interface TaskProgressRepositoryGateway {
    Optional<TaskProgressEntity> findByUserAndTaskAndDate(Long userId, Long taskId, LocalDate date);

    Optional<TaskProgressEntity> findByUserTaskAndDate(Long userId, Long taskId, LocalDate date);

    TaskProgressEntity save(TaskProgressEntity entity);
}