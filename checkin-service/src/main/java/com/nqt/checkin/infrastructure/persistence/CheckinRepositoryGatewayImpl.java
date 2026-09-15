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
    public Optional<CheckinEntity> findByUserTaskProgressAndDate(Long userId, Long taskProgressId, LocalDate date) {
        return jpaRepository.findByUserIdAndTaskProgressIdAndCheckinDate(userId, taskProgressId, date);
    }

    @Override
    public CheckinEntity save(CheckinEntity entity) {
        return jpaRepository.save(entity);
    }
}