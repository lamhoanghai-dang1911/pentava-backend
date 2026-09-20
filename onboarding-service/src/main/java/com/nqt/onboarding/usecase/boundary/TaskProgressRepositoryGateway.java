package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.infrastructure.persistence.TaskProgressEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskProgressRepositoryGateway {
    TaskProgressEntity save(TaskProgressEntity entity);
    Optional<TaskProgressEntity> findById(Long id); // ➕ mới, dùng cho checkin/lưu nháp/lấy tiến độ theo progressId
    Optional<TaskProgressEntity> findByUserTaskAndDate(Long userId, Long taskId, LocalDate date);
    List<Long> findCompletedTaskIdsByUserAndDate(Long userId, LocalDate date);
    List<TaskProgressEntity> findByUserIdAndTaskDate(Long userId, LocalDate date);
    Optional<TaskProgressEntity> findByUserTaskAndDateIncludingDeleted(Long userId, Long taskId, LocalDate date); // dùng cho swap/restore
    List<TaskProgressEntity> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}