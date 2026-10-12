package com.nqt.checkin.infrastructure.persistence;

import com.nqt.checkin.domain.CheckinRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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

    @Override
    public List<CheckinEntity> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByUserIdAndCheckinDateBetweenOrderByCheckinDateAscCreatedAtAsc(userId, startDate, endDate);
    }

    @Override
    public long sumPhotoSizeBytesByUserId(Long userId) {
        if (userId == null) return 0L;
        return jpaRepository.sumPhotoSizeBytesByUserId(userId);
    }

    @Override
    public long countPhotosByUserId(Long userId) {
        if (userId == null) return 0L;
        return jpaRepository.countByUserIdAndImageUrlIsNotNull(userId);
    }
}