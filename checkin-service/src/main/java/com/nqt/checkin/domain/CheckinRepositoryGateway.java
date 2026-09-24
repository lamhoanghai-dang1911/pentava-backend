package com.nqt.checkin.domain;

import com.nqt.checkin.infrastructure.persistence.CheckinEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckinRepositoryGateway {
    Optional<CheckinEntity> findByUserTaskProgressAndDate(Long userId, Long taskProgressId, LocalDate date);
    CheckinEntity save(CheckinEntity entity);
    List<CheckinEntity> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}