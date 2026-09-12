package com.nqt.onboarding.infrastructure.persistence;

import com.nqt.onboarding.usecase.boundary.TaskProgressRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskProgressRepositoryGatewayImpl implements TaskProgressRepositoryGateway {

    private final TaskProgressJpaRepository jpaRepository;

    @Override
    public Optional<TaskProgressEntity> findByUserAndTaskAndDate(Long userId, Long taskId, LocalDate date) {
        return jpaRepository.findByUserIdAndTaskIdAndTaskDate(userId, taskId, date);
    }

    @Override
    public Optional<TaskProgressEntity> findByUserTaskAndDate(Long userId, Long taskId, LocalDate date) {
        return jpaRepository.findByUserIdAndTaskIdAndTaskDate(userId, taskId, date);
    }

    @Override
    public TaskProgressEntity save(TaskProgressEntity entity) {
        return jpaRepository.save(entity);
    }
}