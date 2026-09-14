package com.nqt.onboarding.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTaskProgressRepository extends JpaRepository<TaskProgressEntity, Long> {

    Optional<TaskProgressEntity> findByUserIdAndTaskIdAndTaskDate(Long userId, Long taskId, LocalDate taskDate);

    @Query("SELECT tp.taskId FROM TaskProgressEntity tp WHERE tp.userId = :userId AND tp.taskDate = :date AND tp.isCompleted = true")
    List<Long> findCompletedTaskIdsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}