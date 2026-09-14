package com.nqt.onboarding.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataTaskRepository extends JpaRepository<TaskEntity, Long> {
    @Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId AND (t.weekNumber IS NULL OR t.weekNumber = :weekNumber)")
    List<TaskEntity> findTasksForUserAndWeek(@Param("userId") Long userId, @Param("weekNumber") Integer weekNumber);

    List<TaskEntity> findByUserIdAndGoalIdAndMoodType(Long userId, Long goalId, MoodType moodType); // ➕ mới
}