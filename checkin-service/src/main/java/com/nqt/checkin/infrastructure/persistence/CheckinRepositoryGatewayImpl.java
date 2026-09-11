package com.nqt.checkin.infrastructure.persistence;

import com.nqt.checkin.domain.CheckinRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CheckinRepositoryGatewayImpl implements CheckinRepositoryGateway {

    private final CheckinJpaRepository jpaRepository;

    @Override
    public Optional<CheckinEntity> findByUserTaskAndDate(Long userId, Long taskId, LocalDate date) {
        return jpaRepository.findByUserIdAndTaskIdAndCheckinDate(userId, taskId, date);
    }

    @Override
    public CheckinEntity save(CheckinEntity entity) {
        return jpaRepository.save(entity);
    }
}