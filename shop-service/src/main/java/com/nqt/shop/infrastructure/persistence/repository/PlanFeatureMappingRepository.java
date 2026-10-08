package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.PlanFeatureMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanFeatureMappingRepository extends JpaRepository<PlanFeatureMappingEntity, Long> {

    List<PlanFeatureMappingEntity> findByPlanId(Long planId);

    List<PlanFeatureMappingEntity> findByPlanIdAndIsEnabledTrue(Long planId);

    Optional<PlanFeatureMappingEntity> findByPlanIdAndFeatureId(Long planId, Long featureId);

    Optional<PlanFeatureMappingEntity> findByPlanIdAndFeatureFeatureCodeIgnoreCase(Long planId, String featureCode);

    void deleteByPlanId(Long planId);
}
