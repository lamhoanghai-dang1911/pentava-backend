package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.PlatformMetric;
import com.nqt.manage.infrastructure.persistence.entity.PlatformMetricEntity;
import com.nqt.manage.infrastructure.persistence.repository.PlatformMetricJpaRepository;
import com.nqt.manage.usecase.boundary.PlatformMetricRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlatformMetricRepositoryGatewayImpl implements PlatformMetricRepositoryGateway {

    private final PlatformMetricJpaRepository repository;

    @Override
    public PlatformMetric save(PlatformMetric domain) {
        PlatformMetricEntity entity = toEntity(domain);
        PlatformMetricEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PlatformMetric> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PlatformMetric> findByKey(String metricKey) {
        return repository.findByMetricKey(metricKey).map(this::toDomain);
    }

    @Override
    public List<PlatformMetric> findAll() {
        return repository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformMetric> findActiveMetrics() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private PlatformMetric toDomain(PlatformMetricEntity entity) {
        return PlatformMetric.builder()
                .id(entity.getId())
                .metricKey(entity.getMetricKey())
                .label(entity.getLabel())
                .counterValue(entity.getCounterValue())
                .suffix(entity.getSuffix())
                .icon(entity.getIcon())
                .isAutoCalculated(entity.getIsAutoCalculated())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PlatformMetricEntity toEntity(PlatformMetric domain) {
        return PlatformMetricEntity.builder()
                .id(domain.getId())
                .metricKey(domain.getMetricKey())
                .label(domain.getLabel())
                .counterValue(domain.getCounterValue())
                .suffix(domain.getSuffix())
                .icon(domain.getIcon())
                .isAutoCalculated(domain.getIsAutoCalculated())
                .displayOrder(domain.getDisplayOrder())
                .isActive(domain.getIsActive())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
