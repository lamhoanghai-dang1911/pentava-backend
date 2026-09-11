package com.nqt.checkin.domain;

import com.nqt.checkin.infrastructure.persistence.CheckinEntity;
import java.time.LocalDate;
import java.util.Optional;

public interface CheckinRepositoryGateway {
    Optional<CheckinEntity> findByUserTaskAndDate(Long userId, Long taskId, LocalDate date);
    CheckinEntity save(CheckinEntity entity);
}