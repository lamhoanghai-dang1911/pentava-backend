package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.Banner;
import com.nqt.manage.usecase.boundary.BannerInputPort;
import com.nqt.manage.usecase.boundary.BannerRepositoryGateway;
import com.nqt.manage.usecase.dto.BannerRequestDTO;
import com.nqt.manage.usecase.dto.BannerResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerInteractor implements BannerInputPort {

    private final BannerRepositoryGateway bannerRepositoryGateway;

    @Override
    public BannerResponseDTO createBanner(BannerRequestDTO requestDTO) {
        LocalDateTime now = LocalDateTime.now();
        Banner banner = Banner.builder()
                .title(requestDTO.getTitle())
                .subtitle(requestDTO.getSubtitle())
                .imageUrl(requestDTO.getImageUrl())
                .ctaText(requestDTO.getCtaText())
                .ctaLink(requestDTO.getCtaLink())
                .displayOrder(requestDTO.getDisplayOrder() != null ? requestDTO.getDisplayOrder() : 0)
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toDTO(bannerRepositoryGateway.save(banner));
    }

    @Override
    public BannerResponseDTO updateBanner(Long id, BannerRequestDTO requestDTO) {
        Banner banner = bannerRepositoryGateway.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với ID: " + id));

        banner.setTitle(requestDTO.getTitle());
        banner.setSubtitle(requestDTO.getSubtitle());
        banner.setImageUrl(requestDTO.getImageUrl());
        banner.setCtaText(requestDTO.getCtaText());
        banner.setCtaLink(requestDTO.getCtaLink());
        if (requestDTO.getDisplayOrder() != null) {
            banner.setDisplayOrder(requestDTO.getDisplayOrder());
        }
        if (requestDTO.getIsActive() != null) {
            banner.setIsActive(requestDTO.getIsActive());
        }
        banner.setUpdatedAt(LocalDateTime.now());

        return toDTO(bannerRepositoryGateway.save(banner));
    }

    @Override
    public BannerResponseDTO getBannerById(Long id) {
        return bannerRepositoryGateway.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với ID: " + id));
    }

    @Override
    public List<BannerResponseDTO> getAllBanners() {
        return bannerRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BannerResponseDTO> getActiveBanners() {
        return bannerRepositoryGateway.findActiveBanners().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBanner(Long id) {
        if (bannerRepositoryGateway.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy banner với ID: " + id);
        }
        bannerRepositoryGateway.deleteById(id);
    }

    private BannerResponseDTO toDTO(Banner b) {
        return BannerResponseDTO.builder()
                .id(b.getId())
                .title(b.getTitle())
                .subtitle(b.getSubtitle())
                .imageUrl(b.getImageUrl())
                .ctaText(b.getCtaText())
                .ctaLink(b.getCtaLink())
                .displayOrder(b.getDisplayOrder())
                .isActive(b.getIsActive())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
