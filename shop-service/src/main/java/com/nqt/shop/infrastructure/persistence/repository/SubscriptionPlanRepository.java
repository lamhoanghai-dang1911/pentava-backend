package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.SubscriptionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, Long> {

    Optional<SubscriptionPlanEntity> findByCodeIgnoreCase(String code);

    List<SubscriptionPlanEntity> findAllByOrderByDisplayOrderAsc();

    List<SubscriptionPlanEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
}
