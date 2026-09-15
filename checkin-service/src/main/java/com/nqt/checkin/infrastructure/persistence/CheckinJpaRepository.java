package com.nqt.checkin.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CheckinJpaRepository extends JpaRepository<CheckinEntity, Long> {
    Optional<CheckinEntity> findByUserIdAndTaskProgressIdAndCheckinDate(Long userId, Long taskProgressId, LocalDate checkinDate);
}