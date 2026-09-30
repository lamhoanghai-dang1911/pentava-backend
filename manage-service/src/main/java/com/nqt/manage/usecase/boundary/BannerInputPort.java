package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.BannerRequestDTO;
import com.nqt.manage.usecase.dto.BannerResponseDTO;

import java.util.List;

public interface BannerInputPort {
    BannerResponseDTO createBanner(BannerRequestDTO requestDTO);
    BannerResponseDTO updateBanner(Long id, BannerRequestDTO requestDTO);
    BannerResponseDTO getBannerById(Long id);
    List<BannerResponseDTO> getAllBanners();
    List<BannerResponseDTO> getActiveBanners();
    void deleteBanner(Long id);
}
