package com.nqt.manage.infrastructure.persistence.gateway;

import com.nqt.manage.domain.model.PricingPackage;
import com.nqt.manage.infrastructure.persistence.entity.PricingPackageEntity;
import com.nqt.manage.infrastructure.persistence.repository.PricingPackageJpaRepository;
import com.nqt.manage.usecase.boundary.PricingPackageRepositoryGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PricingPackageRepositoryGatewayImpl implements PricingPackageRepositoryGateway {

    private final PricingPackageJpaRepository repository;

    @Override
    public PricingPackage save(PricingPackage domain) {
        PricingPackageEntity entity = toEntity(domain);
        PricingPackageEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PricingPackage> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<PricingPackage> findAll() {
        return repository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingPackage> findActivePackages() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private PricingPackage toDomain(PricingPackageEntity entity) {
        return PricingPackage.builder()
                .id(entity.getId())
                .name(entity.getName())
                .rubyAmount(entity.getRubyAmount())
                .bonusRuby(entity.getBonusRuby())
                .priceVnd(entity.getPriceVnd())
                .tag(entity.getTag())
                .features(entity.getFeatures())
                .ctaText(entity.getCtaText())
                .ctaLink(entity.getCtaLink())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PricingPackageEntity toEntity(PricingPackage domain) {
        return PricingPackageEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .rubyAmount(domain.getRubyAmount())
                .bonusRuby(domain.getBonusRuby())
                .priceVnd(domain.getPriceVnd())
                .tag(domain.getTag())
                .features(domain.getFeatures())
                .ctaText(domain.getCtaText())
                .ctaLink(domain.getCtaLink())
                .displayOrder(domain.getDisplayOrder())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
