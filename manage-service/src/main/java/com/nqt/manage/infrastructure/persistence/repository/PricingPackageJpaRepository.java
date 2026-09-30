package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.infrastructure.persistence.entity.PricingPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingPackageJpaRepository extends JpaRepository<PricingPackageEntity, Long> {
    List<PricingPackageEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<PricingPackageEntity> findAllByOrderByDisplayOrderAsc();
}
