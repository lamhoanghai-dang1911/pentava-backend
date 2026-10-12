package com.nqt.cinema.infrastructure.persistence;

import com.nqt.cinema.domain.ClipStatus;
import com.nqt.cinema.infrastructure.persistence.entity.ClipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClipJpaRepository extends JpaRepository<ClipEntity, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"photos"})
    Optional<ClipEntity> findById(Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"photos"})
    Optional<ClipEntity> findByIdAndUserId(Long id, Long userId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"photos"})
    List<ClipEntity> findByUserIdAndGoalIdAndWeekNumberOrderByCreatedAtDesc(
            Long userId, Long goalId, int weekNumber);

    long countByUserIdAndGoalIdAndWeekNumberAndStatus(
            Long userId, Long goalId, int weekNumber, ClipStatus status);

    boolean existsByUserIdAndStatusIn(Long userId, List<ClipStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(c.fileSizeBytes), 0) FROM ClipEntity c WHERE c.userId = :userId AND c.status = 'COMPLETED'")
    long sumFileSizeBytesByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    long countByUserIdAndStatus(Long userId, ClipStatus status);
}