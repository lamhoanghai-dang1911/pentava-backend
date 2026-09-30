package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.infrastructure.persistence.entity.PlatformMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformMetricJpaRepository extends JpaRepository<PlatformMetricEntity, Long> {
    Optional<PlatformMetricEntity> findByMetricKey(String metricKey);
    List<PlatformMetricEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<PlatformMetricEntity> findAllByOrderByDisplayOrderAsc();
}
