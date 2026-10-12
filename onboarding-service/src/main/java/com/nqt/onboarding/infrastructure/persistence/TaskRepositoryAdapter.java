package com.nqt.onboarding.infrastructure.persistence;

import com.nqt.onboarding.usecase.boundary.TaskRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepositoryGateway {

    private final SpringDataTaskRepository springDataTaskRepository;

    @Override
    public void saveAll(List<TaskEntity> tasks) {
        springDataTaskRepository.saveAll(tasks);
    }

    @Override
    public List<TaskEntity> findTasksForUserAndWeek(Long userId, Integer weekNumber) {
        return springDataTaskRepository.findTasksForUserAndWeek(userId, weekNumber);
    }

    @Override
    public List<TaskEntity> findByUserIdAndGoalIdAndMoodType(Long userId, Long goalId, MoodType moodType) {
        return springDataTaskRepository.findByUserIdAndGoalIdAndMoodType(userId, goalId, moodType);
    }

    @Override
    public List<TaskEntity> findByIds(List<Long> taskIds) {
        return springDataTaskRepository.findAllById(taskIds);
    }

    @Override
    public List<TaskEntity> findTasksForGoalAndWeek(Long goalId, Integer weekNumber) {
        return springDataTaskRepository.findByGoalIdAndWeekNumberOrderByIdAsc(goalId, weekNumber);
    }

    @Override
    public List<TaskEntity> findAllByGoalId(Long goalId) {
        return springDataTaskRepository.findByGoalIdOrderByIdAsc(goalId);
    }

    @Override
    public List<TaskEntity> findByUserIdOrderByGoalIdDescWeekNumberAscIdAsc(Long userId) {
        return springDataTaskRepository.findByUserIdOrderByGoalIdDescWeekNumberAscIdAsc(userId);
    }
}