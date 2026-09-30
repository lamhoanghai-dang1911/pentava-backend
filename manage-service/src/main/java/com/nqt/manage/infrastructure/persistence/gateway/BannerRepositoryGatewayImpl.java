package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.Banner;
import com.nqt.manage.infrastructure.persistence.entity.BannerEntity;
import com.nqt.manage.infrastructure.persistence.repository.BannerJpaRepository;
import com.nqt.manage.usecase.boundary.BannerRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BannerRepositoryGatewayImpl implements BannerRepositoryGateway {

    private final BannerJpaRepository repository;

    @Override
    public Banner save(Banner domain) {
        BannerEntity entity = toEntity(domain);
        BannerEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Banner> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Banner> findAll() {
        return repository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Banner> findActiveBanners() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private Banner toDomain(BannerEntity entity) {
        return Banner.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .imageUrl(entity.getImageUrl())
                .ctaText(entity.getCtaText())
                .ctaLink(entity.getCtaLink())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private BannerEntity toEntity(Banner domain) {
        return BannerEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .subtitle(domain.getSubtitle())
                .imageUrl(domain.getImageUrl())
                .ctaText(domain.getCtaText())
                .ctaLink(domain.getCtaLink())
                .displayOrder(domain.getDisplayOrder())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
