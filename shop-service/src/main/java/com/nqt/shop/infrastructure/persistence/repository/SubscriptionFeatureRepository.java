package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.SubscriptionFeatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionFeatureRepository extends JpaRepository<SubscriptionFeatureEntity, Long> {

    Optional<SubscriptionFeatureEntity> findByFeatureCodeIgnoreCase(String featureCode);

    boolean existsByFeatureCodeIgnoreCase(String featureCode);

    List<SubscriptionFeatureEntity> findAllByOrderByDisplayOrderAsc();
}
