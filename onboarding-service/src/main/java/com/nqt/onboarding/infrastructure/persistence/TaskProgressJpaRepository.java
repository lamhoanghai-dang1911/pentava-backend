package com.nqt.onboarding.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface TaskProgressJpaRepository extends JpaRepository<TaskProgressEntity, Long> {
    Optional<TaskProgressEntity> findByUserIdAndTaskIdAndTaskDate(Long userId, Long taskId, LocalDate taskDate);
}