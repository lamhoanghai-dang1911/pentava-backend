package com.nqt.onboarding.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskProgressJpaRepository extends JpaRepository<TaskProgressEntity, Long> {

    // ➕ dùng để tìm cả row đã xoá mềm (phục vụ restore khi đổi qua đổi lại)
    Optional<TaskProgressEntity> findByUserIdAndTaskIdAndTaskDate(Long userId, Long taskId, LocalDate taskDate);

    // Các query nghiệp vụ chính -> chỉ lấy row chưa bị xoá mềm
    Optional<TaskProgressEntity> findByUserIdAndTaskIdAndTaskDateAndIsDeletedFalse(Long userId, Long taskId, LocalDate taskDate);

    List<TaskProgressEntity> findByUserIdAndTaskDateAndIsCompletedTrueAndIsDeletedFalse(Long userId, LocalDate taskDate);

    List<TaskProgressEntity> findByUserIdAndTaskDateAndIsDeletedFalse(Long userId, LocalDate taskDate);
    List<TaskProgressEntity> findByUserIdAndTaskDateBetweenAndIsDeletedFalseOrderByTaskDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT tp.taskId FROM TaskProgressEntity tp WHERE tp.userId = :userId AND tp.isCompleted = true AND tp.isDeleted = false")
    List<Long> findDistinctCompletedTaskIdsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT tp.taskDate FROM TaskProgressEntity tp WHERE tp.userId = :userId AND tp.taskDate < :today AND tp.isDeleted = false ORDER BY tp.taskDate DESC")
    List<LocalDate> findDistinctTaskDatesBeforeDate(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("today") LocalDate today);
}