package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.PricingPackage;
import com.nqt.manage.usecase.boundary.PricingPackageInputPort;
import com.nqt.manage.usecase.boundary.PricingPackageRepositoryGateway;
import com.nqt.manage.usecase.dto.PricingPackageRequestDTO;
import com.nqt.manage.usecase.dto.PricingPackageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingPackageInteractor implements PricingPackageInputPort {

    private final PricingPackageRepositoryGateway pricingPackageRepositoryGateway;

    @Override
    public PricingPackageResponseDTO createPricingPackage(PricingPackageRequestDTO requestDTO) {
        LocalDateTime now = LocalDateTime.now();
        PricingPackage pkg = PricingPackage.builder()
                .name(requestDTO.getName())
                .rubyAmount(requestDTO.getRubyAmount())
                .bonusRuby(requestDTO.getBonusRuby() != null ? requestDTO.getBonusRuby() : 0)
                .priceVnd(requestDTO.getPriceVnd())
                .tag(requestDTO.getTag())
                .features(requestDTO.getFeatures())
                .ctaText(requestDTO.getCtaText() != null ? requestDTO.getCtaText() : "Nạp ngay")
                .ctaLink(requestDTO.getCtaLink() != null ? requestDTO.getCtaLink() : "/shop")
                .displayOrder(requestDTO.getDisplayOrder() != null ? requestDTO.getDisplayOrder() : 0)
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toDTO(pricingPackageRepositoryGateway.save(pkg));
    }

    @Override
    public PricingPackageResponseDTO updatePricingPackage(Long id, PricingPackageRequestDTO requestDTO) {
        PricingPackage pkg = pricingPackageRepositoryGateway.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói nạp với ID: " + id));

        pkg.setName(requestDTO.getName());
        pkg.setRubyAmount(requestDTO.getRubyAmount());
        if (requestDTO.getBonusRuby() != null) {
            pkg.setBonusRuby(requestDTO.getBonusRuby());
        }
        pkg.setPriceVnd(requestDTO.getPriceVnd());
        pkg.setTag(requestDTO.getTag());
        pkg.setFeatures(requestDTO.getFeatures());
        if (requestDTO.getCtaText() != null) {
            pkg.setCtaText(requestDTO.getCtaText());
        }
        if (requestDTO.getCtaLink() != null) {
            pkg.setCtaLink(requestDTO.getCtaLink());
        }
        if (requestDTO.getDisplayOrder() != null) {
            pkg.setDisplayOrder(requestDTO.getDisplayOrder());
        }
        if (requestDTO.getIsActive() != null) {
            pkg.setIsActive(requestDTO.getIsActive());
        }
        pkg.setUpdatedAt(LocalDateTime.now());

        return toDTO(pricingPackageRepositoryGateway.save(pkg));
    }

    @Override
    public PricingPackageResponseDTO getPricingPackageById(Long id) {
        return pricingPackageRepositoryGateway.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói nạp với ID: " + id));
    }

    @Override
    public List<PricingPackageResponseDTO> getAllPricingPackages() {
        return pricingPackageRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingPackageResponseDTO> getActivePricingPackages() {
        return pricingPackageRepositoryGateway.findActivePackages().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePricingPackage(Long id) {
        if (pricingPackageRepositoryGateway.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy gói nạp với ID: " + id);
        }
        pricingPackageRepositoryGateway.deleteById(id);
    }

    private PricingPackageResponseDTO toDTO(PricingPackage p) {
        return PricingPackageResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .rubyAmount(p.getRubyAmount())
                .bonusRuby(p.getBonusRuby())
                .priceVnd(p.getPriceVnd())
                .tag(p.getTag())
                .features(p.getFeatures())
                .ctaText(p.getCtaText())
                .ctaLink(p.getCtaLink())
                .displayOrder(p.getDisplayOrder())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
