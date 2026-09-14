package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.infrastructure.persistence.MoodType;
import com.nqt.onboarding.infrastructure.persistence.TaskEntity;
import java.util.List;

public interface TaskRepositoryGateway {
    void saveAll(List<TaskEntity> tasks);
    List<TaskEntity> findTasksForUserAndWeek(Long userId, Integer weekNumber);
    List<TaskEntity> findByUserIdAndGoalIdAndMoodType(Long userId, Long goalId, MoodType moodType);
    List<TaskEntity> findByIds(List<Long> taskIds); // ➕ mới, dùng để build lại TaskResponseDTO từ danh sách taskId đã chọn
}