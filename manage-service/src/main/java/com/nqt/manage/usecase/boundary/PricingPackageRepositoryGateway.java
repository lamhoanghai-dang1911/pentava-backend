package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.PricingPackage;

import java.util.List;
import java.util.Optional;

public interface PricingPackageRepositoryGateway {
    PricingPackage save(PricingPackage pricingPackage);
    Optional<PricingPackage> findById(Long id);
    List<PricingPackage> findAll();
    List<PricingPackage> findActivePackages();
    void deleteById(Long id);
}
