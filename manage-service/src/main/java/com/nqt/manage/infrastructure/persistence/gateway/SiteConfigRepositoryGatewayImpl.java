package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.SiteConfig;
import com.nqt.manage.infrastructure.persistence.entity.SiteConfigEntity;
import com.nqt.manage.infrastructure.persistence.repository.SiteConfigJpaRepository;
import com.nqt.manage.usecase.boundary.SiteConfigRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SiteConfigRepositoryGatewayImpl implements SiteConfigRepositoryGateway {

    private final SiteConfigJpaRepository repository;

    @Override
    public SiteConfig save(SiteConfig domain) {
        SiteConfigEntity entity = toEntity(domain);
        SiteConfigEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SiteConfig> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<SiteConfig> findByKey(String configKey) {
        return repository.findByConfigKey(configKey).map(this::toDomain);
    }

    @Override
    public List<SiteConfig> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteConfig> findPublicConfigs() {
        return repository.findByIsPublicTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private SiteConfig toDomain(SiteConfigEntity entity) {
        return SiteConfig.builder()
                .id(entity.getId())
                .configKey(entity.getConfigKey())
                .configValue(entity.getConfigValue())
                .groupName(entity.getGroupName())
                .description(entity.getDescription())
                .isPublic(entity.getIsPublic())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SiteConfigEntity toEntity(SiteConfig domain) {
        return SiteConfigEntity.builder()
                .id(domain.getId())
                .configKey(domain.getConfigKey())
                .configValue(domain.getConfigValue())
                .groupName(domain.getGroupName())
                .description(domain.getDescription())
                .isPublic(domain.getIsPublic())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
