package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.PricingPackageRequestDTO;
import com.nqt.manage.usecase.dto.PricingPackageResponseDTO;

import java.util.List;

public interface PricingPackageInputPort {
    PricingPackageResponseDTO createPricingPackage(PricingPackageRequestDTO requestDTO);
    PricingPackageResponseDTO updatePricingPackage(Long id, PricingPackageRequestDTO requestDTO);
    PricingPackageResponseDTO getPricingPackageById(Long id);
    List<PricingPackageResponseDTO> getAllPricingPackages();
    List<PricingPackageResponseDTO> getActivePricingPackages();
    void deletePricingPackage(Long id);
}
