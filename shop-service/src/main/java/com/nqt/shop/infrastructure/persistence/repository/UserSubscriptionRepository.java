package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.UserSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, Long> {

    List<UserSubscriptionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserSubscriptionEntity> findFirstByUserIdAndStatusAndEndDateAfterOrderByEndDateDesc(
            Long userId, String status, LocalDateTime now);

    List<UserSubscriptionEntity> findByStatusAndEndDateBefore(String status, LocalDateTime now);

    List<UserSubscriptionEntity> findAllByOrderByCreatedAtDesc();
}
