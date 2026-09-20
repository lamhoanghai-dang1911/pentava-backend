package com.nqt.onboarding.infrastructure.persistence;

import com.nqt.onboarding.usecase.boundary.TaskProgressRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskProgressRepositoryGatewayImpl implements TaskProgressRepositoryGateway {

    private final TaskProgressJpaRepository jpaRepository;

    @Override
    public TaskProgressEntity save(TaskProgressEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<TaskProgressEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<TaskProgressEntity> findByUserTaskAndDate(Long userId, Long taskId, LocalDate date) {
        return jpaRepository.findByUserIdAndTaskIdAndTaskDateAndIsDeletedFalse(userId, taskId, date);
    }

    @Override
    public List<Long> findCompletedTaskIdsByUserAndDate(Long userId, LocalDate date) {
        return jpaRepository.findByUserIdAndTaskDateAndIsCompletedTrueAndIsDeletedFalse(userId, date)
                .stream().map(TaskProgressEntity::getTaskId).toList();
    }

    @Override
    public List<TaskProgressEntity> findByUserIdAndTaskDate(Long userId, LocalDate date) {
        return jpaRepository.findByUserIdAndTaskDateAndIsDeletedFalse(userId, date);
    }

    @Override
    public Optional<TaskProgressEntity> findByUserTaskAndDateIncludingDeleted(Long userId, Long taskId, LocalDate date) {
        return jpaRepository.findByUserIdAndTaskIdAndTaskDate(userId, taskId, date);
    }

    @Override
    public List<TaskProgressEntity> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByUserIdAndTaskDateBetweenAndIsDeletedFalseOrderByTaskDateDesc(userId, startDate, endDate);
    }
}