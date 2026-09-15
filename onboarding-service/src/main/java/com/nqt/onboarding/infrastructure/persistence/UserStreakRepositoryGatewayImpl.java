package com.nqt.onboarding.infrastructure.persistence;

import com.nqt.onboarding.usecase.boundary.UserStreakRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserStreakRepositoryGatewayImpl implements UserStreakRepositoryGateway {

    private final UserStreakJpaRepository jpaRepository;

    @Override
    public Optional<UserStreakEntity> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public UserStreakEntity save(UserStreakEntity entity) {
        return jpaRepository.save(entity);
    }
}