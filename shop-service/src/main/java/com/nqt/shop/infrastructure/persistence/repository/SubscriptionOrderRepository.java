package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.domain.model.TopupStatus;
import com.nqt.shop.infrastructure.persistence.entity.SubscriptionOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionOrderRepository extends JpaRepository<SubscriptionOrderEntity, Long> {

    Optional<SubscriptionOrderEntity> findByTransactionCode(String transactionCode);

    List<SubscriptionOrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SubscriptionOrderEntity> findByStatusOrderByCreatedAtDesc(TopupStatus status);
}
