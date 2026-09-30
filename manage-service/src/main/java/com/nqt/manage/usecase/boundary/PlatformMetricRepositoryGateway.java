package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.PlatformMetric;

import java.util.List;
import java.util.Optional;

public interface PlatformMetricRepositoryGateway {
    PlatformMetric save(PlatformMetric metric);
    Optional<PlatformMetric> findById(Long id);
    Optional<PlatformMetric> findByKey(String metricKey);
    List<PlatformMetric> findAll();
    List<PlatformMetric> findActiveMetrics();
    void deleteById(Long id);
}
