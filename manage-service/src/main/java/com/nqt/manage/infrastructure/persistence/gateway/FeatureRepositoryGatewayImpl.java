package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.Feature;
import com.nqt.manage.infrastructure.persistence.entity.FeatureEntity;
import com.nqt.manage.infrastructure.persistence.repository.FeatureJpaRepository;
import com.nqt.manage.usecase.boundary.FeatureRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeatureRepositoryGatewayImpl implements FeatureRepositoryGateway {

    private final FeatureJpaRepository repository;

    @Override
    public Feature save(Feature domain) {
        FeatureEntity entity = toEntity(domain);
        FeatureEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Feature> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Feature> findAll() {
        return repository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Feature> findActiveFeatures() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private Feature toDomain(FeatureEntity entity) {
        return Feature.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .tag(entity.getTag())
                .previewImageUrl(entity.getPreviewImageUrl())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private FeatureEntity toEntity(Feature domain) {
        return FeatureEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .icon(domain.getIcon())
                .tag(domain.getTag())
                .previewImageUrl(domain.getPreviewImageUrl())
                .displayOrder(domain.getDisplayOrder())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
