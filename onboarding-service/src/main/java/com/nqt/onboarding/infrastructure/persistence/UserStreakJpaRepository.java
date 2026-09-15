package com.nqt.onboarding.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserStreakJpaRepository extends JpaRepository<UserStreakEntity, Long> {
    Optional<UserStreakEntity> findByUserId(Long userId);
}