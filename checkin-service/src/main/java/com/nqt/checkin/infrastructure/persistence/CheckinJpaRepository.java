package com.nqt.checkin.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface CheckinJpaRepository extends JpaRepository<CheckinEntity, Long> {
    Optional<CheckinEntity> findByUserIdAndTaskIdAndCheckinDate(Long userId, Long taskId, LocalDate checkinDate);
}