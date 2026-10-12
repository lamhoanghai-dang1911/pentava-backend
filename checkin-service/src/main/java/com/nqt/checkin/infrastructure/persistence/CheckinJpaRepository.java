package com.nqt.checkin.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckinJpaRepository extends JpaRepository<CheckinEntity, Long> {
    Optional<CheckinEntity> findByUserIdAndTaskProgressIdAndCheckinDate(Long userId, Long taskProgressId, LocalDate checkinDate);
    List<CheckinEntity> findByUserIdAndCheckinDateBetweenOrderByCheckinDateAscCreatedAtAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(COALESCE(c.fileSizeBytes, 1572864)), 0) FROM CheckinEntity c WHERE c.userId = :userId AND c.imageUrl IS NOT NULL AND TRIM(c.imageUrl) != ''")
    long sumPhotoSizeBytesByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    long countByUserIdAndImageUrlIsNotNull(Long userId);
}