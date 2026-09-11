package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.infrastructure.persistence.TaskEntity;

import java.util.List;

public interface TaskRepositoryGateway {
    void saveAll(List<TaskEntity> tasks);
    List<TaskEntity> findTasksForUserAndWeek(Long userId, Integer weekNumber); // ➕ Thêm method này
}
