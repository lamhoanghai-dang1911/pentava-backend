package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.infrastructure.persistence.TaskProgressEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskProgressRepositoryGateway {
    TaskProgressEntity save(TaskProgressEntity entity);
    Optional<TaskProgressEntity> findByUserTaskAndDate(Long userId, Long taskId, LocalDate date);
    List<Long> findCompletedTaskIdsByUserAndDate(Long userId, LocalDate date);
    List<TaskProgressEntity> findByUserIdAndTaskDate(Long userId, LocalDate date);
    Optional<TaskProgressEntity> findByUserTaskAndDateIncludingDeleted(Long userId, Long taskId, LocalDate date); // ➕ mới, dùng cho swap/restore
}