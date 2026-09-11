package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.infrastructure.persistence.TaskProgressEntity;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskProgressRepositoryGateway {
    Optional<TaskProgressEntity> findByUserTaskAndDate(Long userId, Long taskId, LocalDate taskDate);
    TaskProgressEntity save(TaskProgressEntity entity);
}
