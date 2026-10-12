package com.nqt.onboarding.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserStreakFreezeJpaRepository extends JpaRepository<UserStreakFreezeEntity, Long> {
    long countByUserIdAndYearMonth(Long userId, String yearMonth);
    boolean existsByUserIdAndFreezeDate(Long userId, LocalDate freezeDate);
    List<UserStreakFreezeEntity> findByUserIdOrderByFreezeDateDesc(Long userId);
}
